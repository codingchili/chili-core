package com.codingchili.core.context;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.configuration.system.SystemSettings;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.*;
import com.codingchili.core.listener.transport.ClusterListener;
import com.codingchili.core.logging.Level;
import com.codingchili.core.logging.LogMessage;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.logging.RemoteLogger;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.MetricsService;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static com.codingchili.core.configuration.CoreStrings.getUnsupportedDeployment;


/**
 * @author Robin Duda
 * <p>
 * Implementation of the CoreContext, each context gets its own worker pool.
 */
public class SystemContext implements CoreContext {
    private static AtomicBoolean initialized = new AtomicBoolean(false);
    protected Vertx vertx;
    private Map<String, List<String>> deployments = new HashMap<>();
    private WorkerExecutor executor;
    private RemoteLogger logger;

    /**
     * Creates a new system context that shares vertx instance with the given context.
     *
     * @param context context to clone vertx instance from.
     */
    protected SystemContext(CoreContext context) {
        this(context.vertx());
    }

    /**
     * Creates a new vertx instance to be used for this context.
     */
    public SystemContext() {
        this(Vertx.vertx(Configurations.system().getOptions()));
    }


    private SystemContext(Vertx vertx) {
        this.vertx = vertx;
        this.logger = new RemoteLogger(this, SystemContext.class);
        initialize();
    }

    /**
     * Creates a clustered instance of a context.
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
        executor = vertx.createSharedWorkerExecutor("systemcontext", system().getWorkerPoolSize());
        vertx.exceptionHandler(throwable -> logger.onError(throwable));

        if (!initialized.get()) {
            MetricsService metrics = MetricsService.create(vertx);

            periodic(this::getMetricTimer, CoreStrings.LOG_METRICS, handler -> {
                if (system().isMetrics()) {
                    JsonObject json = metrics.getMetricsSnapshot(vertx);
                    this.onMetricsSnapshot(json);
                }
            });
            StartupListener.publish(this);
            initialized.set(true);
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
    public void periodic(TimerSource timeout, String name, Handler<Long> handler) {
        final int initial = timeout.getMS();

        vertx.setPeriodic(timeout.getMS(), event -> {
            if (timeout.getMS() != initial) {
                vertx.cancelTimer(event);

                if (timeout.getMS() > 0) {
                    periodic(timeout, name, handler);
                }
                logger.onTimerSourceChanged(name, initial, timeout.getMS());
            }
            handler.handle(event);
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
        Supplier<Object> deployment = () -> {
            try {
                return Class.forName(target).getConstructor().newInstance();
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                    IllegalAccessException | ClassNotFoundException e) {
                throw new CoreRuntimeException(e.getMessage());
            }
        };

        if (deployment.get() instanceof CoreHandler) {
            return handler(() -> (CoreHandler) deployment.get());
        } else if (deployment.get() instanceof CoreListener) {
            return listener(() -> {
                CoreListener listener = (CoreListener) deployment.get();
                listener.handler(new BusRouter());
                listener.settings(ListenerSettings::new);
                return listener;
            });
        } else if (deployment.get() instanceof CoreService) {
            return service(() -> (CoreService) deployment.get());
        } else if (deployment.get() instanceof Verticle) {
            return deployN(target);
        } else {
            return Future.failedFuture(getUnsupportedDeployment(target));
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
                .settings(() -> settings)
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
    public <T> void blocking(Handler<Future<T>> sync, Handler<AsyncResult<T>> result) {
        blocking(sync, false, result);
    }

    @Override
    public <T> void blocking(Handler<Future<T>> sync, boolean ordered, Handler<AsyncResult<T>> result) {
        executor.executeBlocking(sync, ordered, result);
    }

    @Override
    public Logger logger(Class aClass) {
        return new RemoteLogger(this, aClass);
    }

    @Override
    public void close() {
        close(closed -> { });
    }

    @Override
    public void close(Handler<AsyncResult<Void>> handler) {
        initialized.set(false);
        vertx.close((close) -> {
            StartupListener.publish(null);
            ShutdownListener.publish(this);
            handler.handle(Future.succeededFuture());
        });
    }

    @Override
    public Vertx vertx() {
        return vertx;
    }

    protected LogMessage event(String event) {
        return event(event, Level.INFO);
    }

    protected LogMessage event(String event, Level level) {
        return logger.event(event, level);
    }
}
