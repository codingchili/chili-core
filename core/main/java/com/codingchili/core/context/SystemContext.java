package com.codingchili.core.context;

import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.impl.VertxImpl;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.MetricsService;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.configuration.system.SystemSettings;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.*;
import com.codingchili.core.listener.transport.ClusterListener;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.logging.RemoteLogger;

import static com.codingchili.core.configuration.CoreStrings.getUnsupportedDeployment;


/**
 * Implementation of the CoreContext, each context gets its own worker pool.
 */
public class SystemContext implements CoreContext {
    private static AtomicBoolean initialized = new AtomicBoolean(false);
    private Map<String, List<String>> deployments = new HashMap<>();
    private MetricsService metrics;
    private RemoteLogger logger;
    protected Vertx vertx;

    /**
     * Creates a new vertx instance to be used for this context.
     */
    public SystemContext() {
        this(Vertx.vertx(getOptions()));
    }

    private static VertxOptions getOptions() {
        VertxOptions options = Configurations.system().getOptions();
        options.getEventBusOptions().setClustered(false);
        return options;
    }

    /**
     * Creates a new system context that shares vertx instance with the given context.
     *
     * @param context context to clone vertx instance from.
     */
    protected SystemContext(CoreContext context) {
        this(context.vertx());
    }


    private SystemContext(Vertx vertx) {
        this.vertx = vertx;
        this.logger = new RemoteLogger(this, SystemContext.class);

        // add a shutdown hook for gracefully shutting down the context.
        ShutdownHook.register(this);

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

    private void initialize() {
        vertx.exceptionHandler(throwable -> logger.onError(throwable));

        if (!initialized.get()) {
            this.metrics = MetricsService.create(vertx);
            var timer = TimerSource.of(getMetricTimer())
                    .setName(CoreStrings.LOG_METRICS);

            periodic(timer, handler -> this.processMetrics());
            StartupListener.publish(this);
            initialized.set(true);
        }
    }

    private void processMetrics() {
        SystemSettings system = system();

        if (system.getMetrics().isEnabled()) {
            var filters = system.getMetrics().getFilters();

            if (filters.isEmpty()) {
                this.onMetricsSnapshot(metrics.getMetricsSnapshot(vertx));
            } else {
                final JsonObject root = new JsonObject();
                filters.forEach(filter -> {
                    var capture = metrics.getMetricsSnapshot(filter.getPath());
                    filter.apply(capture, root);
                });
                this.onMetricsSnapshot(root);
            }
        }
    }

    private int getMetricTimer() {
        return system().getMetricRate();
    }

    protected void onMetricsSnapshot(JsonObject json) {
        if (json != null) {
            logger.onMetricsSnapshot(json);
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
        Future<String> future = Future.future();
        vertx.deployVerticle(verticle, new DeploymentOptions().setInstances(system().getHandlers()), future);
        return future;
    }

    @Override
    public Future<String> handler(Supplier<CoreHandler> handler) {
        ListenerSettings settings = new ListenerSettings();
        Future<String> future = Future.future();
        deployN(() -> new ClusterListener()
                .settings(settings)
                .handler(handler.get()), future);
        return future;
    }

    @Override
    public Future<String> listener(Supplier<CoreListener> listener) {
        Future<String> future = Future.future();
        deployN(listener::get, future);
        return future;
    }

    @Override
    public Future<String> service(Supplier<CoreService> service) {
        Future<String> future = Future.future();
        deployN(service::get, future);
        return future;
    }

    private void deployN(Supplier<CoreDeployment> supplier, Future<String> done) {
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
    public void stop(String deploymentId) {
        if (deployments.containsKey(deploymentId)) {
            deployments.get(deploymentId).forEach(vertx::undeploy);
        } else {
            vertx.undeploy(deploymentId);
        }
    }

    @Override
    public Future<CompositeFuture> stop() {
        List<Future> futures = deployments.values()
                .stream()
                .flatMap(Collection::stream)
                .map((id) -> {
                    Future<Void> future = Future.future();
                    vertx.undeploy(id, future);
                    return future;
                })
                .collect(Collectors.toList());
        return CompositeFuture.all(futures);
    }

    ExecutorService getBlockingExecutor() {
        return ((VertxImpl) vertx).getWorkerPool();
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
