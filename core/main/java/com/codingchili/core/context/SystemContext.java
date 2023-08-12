package com.codingchili.core.context;

import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.impl.VertxImpl;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.codingchili.core.configuration.system.SystemSettings;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.*;
import com.codingchili.core.listener.transport.ClusterListener;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.logging.RemoteLogger;
import com.codingchili.core.metrics.MetricCollector;
import com.codingchili.core.metrics.MetricSettings;

import static com.codingchili.core.configuration.CoreStrings.getUnsupportedDeployment;


/**
 * Implementation of the CoreContext, each context gets its own worker pool.
 */
public class SystemContext implements CoreContext {
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private final Map<String, List<String>> deployments = new HashMap<>();
    private MetricCollector metrics;
    private RemoteLogger logger;
    protected Vertx vertx;

    /**
     * Creates a new vertx instance to be used for this context.
     */
    public SystemContext() {
        this(Vertx.vertx(getOptions()));
    }

    private static VertxOptions getOptions() {
        return Configurations.system().getOptions();
    }

    /**
     * Creates a new system context that shares vertx instance with the given context.
     *
     * @param context context to clone vertx instance from.
     */
    protected SystemContext(CoreContext context) {
        this.vertx = context.vertx();
        this.metrics = context.metrics();
    }

    private SystemContext(Vertx vertx) {
        this.vertx = vertx;
        initialize();
    }

    /**
     * Creates a clustered instance of a context.
     *
     * @param handler called with the context on creation.
     */
    public static void clustered(Handler<AsyncResult<CoreContext>> handler) {
        Vertx.clusteredVertx(Configurations.system().getOptions(), cluster -> {
            if (cluster.succeeded()) {
                handler.handle(Future.succeededFuture(new SystemContext(cluster.result())));
            } else {
                handler.handle(Future.failedFuture(cluster.cause()));
            }
        });
    }

    @Override
    public MetricCollector metrics() {
        return metrics;
    }

    @Override
    public MetricCollector metrics(String registryName) {
        return new MetricCollector(
                this,
                new MetricSettings().setEnabled(true),
                registryName
        );
    }

    private void initialize() {
        this.logger = new RemoteLogger(this, SystemContext.class);

        // add a shutdown hook for gracefully shutting down the context.
        ShutdownHook.register(this);

        vertx.exceptionHandler(throwable -> logger.onError(throwable));

        if (!initialized.get()) {
            this.metrics = new MetricCollector(
                    this,
                    Configurations.system().getMetrics(),
                    MetricSettings.REGISTRY_NAME
            );
            StartupListener.publish(this);
            initialized.set(true);
        }
    }

    @Override
    public EventBus bus() {
        return vertx.eventBus();
    }

    @Override
    public SystemSettings system() {
        return Configurations.system();
    }

    @Override
    public void periodic(TimerSource timeout, Handler<Long> handler) {
        final int initial = timeout.getMS();

        vertx.setPeriodic(timeout.getMS(), event -> {
            if (timeout.getMS() != initial) {
                vertx.cancelTimer(event);

                if (!timeout.isTerminated()) {
                    periodic(timeout, handler);
                }
                logger.onTimerSourceChanged(timeout.getName(), initial, timeout.getMS());
            }

            if (!timeout.isPaused()) {
                handler.handle(event);
            }
        });
    }

    @Override
    public long timer(long ms, Handler<Long> handler) {
        return vertx.setTimer(ms, handler);
    }

    @Override
    public void cancel(long timer) {
        vertx.cancelTimer(timer);
    }

    @Override
    public Future<String> deploy(String target) {
        try {
            Class<?> theClass = Class.forName(target);

            Supplier<Object> deployment = () -> {
                try {
                    return theClass.getConstructor().newInstance();
                } catch (Exception e) {
                    throw new CoreRuntimeException(e.getMessage());
                }
            };

            if (CoreHandler.class.isAssignableFrom(theClass)) {
                return handler(() -> (CoreHandler) deployment.get());
            } else if (CoreListener.class.isAssignableFrom(theClass)) {
                return listener(() -> {
                    CoreListener listener = (CoreListener) deployment.get();
                    listener.handler(new BusRouter());
                    listener.settings(new ListenerSettings());
                    return listener;
                });
            } else if (CoreService.class.isAssignableFrom(theClass)) {
                return service(() -> (CoreService) deployment.get());
            } else if (Verticle.class.isAssignableFrom(theClass)) {
                return deployN(target);
            } else {
                return Future.failedFuture(getUnsupportedDeployment(target));
            }
        } catch (ClassNotFoundException e) {
            throw new CoreRuntimeException(e.getMessage());
        }
    }

    private Future<String> deployN(String verticle) {
        Promise<String> promise = Promise.promise();
        vertx.deployVerticle(verticle, new DeploymentOptions().setInstances(system().getHandlers()), promise);
        return promise.future();
    }

    @Override
    public Future<String> handler(Supplier<CoreHandler> handler) {
        ListenerSettings settings = new ListenerSettings();
        Promise<String> promise = Promise.promise();
        deployN(() -> new ClusterListener()
                .settings(settings)
                .handler(handler.get()), promise);
        return promise.future();
    }

    @Override
    public Future<String> listener(Supplier<CoreListener> listener) {
        Promise<String> promise = Promise.promise();
        deployN(listener::get, promise);
        return promise.future();
    }

    @Override
    public Future<String> service(Supplier<CoreService> service) {
        Promise<String> promise = Promise.promise();
        deployN(service::get, promise);
        return promise.future();
    }

    private void deployN(Supplier<CoreDeployment> supplier, Promise<String> done) {
        CoreDeployment deployment = supplier.get();
        int handlerCount = getHandlerCount(deployment);
        CountDownLatch latch = new CountDownLatch(handlerCount);
        String deploymentId = UUID.randomUUID().toString();
        List<String> completed = new ArrayList<>();

        for (int i = 0; i < handlerCount; i++) {
            CoreVerticle verticle = new CoreVerticle(deployment, this);
            vertx.deployVerticle(verticle, deployed -> {
                if (deployed.succeeded()) {
                    completed.add(deployed.result());
                    latch.countDown();
                    if (latch.getCount() == 0) {
                        done.complete(deploymentId);
                        deployments.put(deploymentId, completed);
                    }
                } else {
                    done.tryFail(deployed.cause());
                }
            });
            if (i < getHandlerCount(deployment) - 1)
                deployment = supplier.get();
        }
    }

    private int getHandlerCount(CoreDeployment deployable) {
        if (deployable instanceof DeploymentAware) {
            return ((DeploymentAware) deployable).instances();
        } else if (deployable instanceof CoreListener) {
            return system().getListeners();
        } else if (deployable instanceof CoreService) {
            return system().getServices();
        } else {
            return system().getHandlers();
        }
    }

    @Override
    public Future<Void> stop(String deploymentId) {
        if (deployments.containsKey(deploymentId)) {
            var promises = new ArrayList<Future>();
            deployments.get(deploymentId).forEach(deployment -> {
                promises.add(vertx.undeploy(deployment));
            });
            return CompositeFuture.all(promises).mapEmpty();
        } else {
            return vertx.undeploy(deploymentId);
        }
    }

    @Override
    public Future<CompositeFuture> stop() {
        List<Future> futures = deployments.values()
                .stream()
                .flatMap(Collection::stream)
                .map((id) -> {
                    Promise<Void> promise = Promise.promise();
                    vertx.undeploy(id, promise);
                    return promise.future();
                })
                .collect(Collectors.toList());
        return CompositeFuture.all(futures);
    }

    ExecutorService getBlockingExecutor() {
        return ((VertxImpl) vertx).getWorkerPool().executor();
    }

    @Override
    public <T> void blocking(Handler<Promise<T>> sync, Handler<AsyncResult<T>> result) {
        blocking(sync, false, result);
    }

    @Override
    public <T> void blocking(Handler<Promise<T>> sync, boolean ordered, Handler<AsyncResult<T>> result) {
        vertx.executeBlocking(sync, ordered, result);
    }

    @Override
    public Logger logger(Class aClass) {
        return new RemoteLogger(this, aClass);
    }

    @Override
    public void close() {
        close(closed -> {

        });
    }

    @Override
    public void close(Handler<AsyncResult<Void>> handler) {
        initialized.set(false);
        vertx.close((close) -> {
            handler.handle(Future.succeededFuture());
        });
    }

    @Override
    public Vertx vertx() {
        return vertx;
    }
}