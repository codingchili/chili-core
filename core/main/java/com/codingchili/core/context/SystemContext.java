package com.codingchili.core.context;

import com.codingchili.core.configuration.system.SystemSettings;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.*;
import com.codingchili.core.listener.transport.ClusterListener;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.logging.RemoteLogger;
import com.codingchili.core.metrics.MetricCollector;
import com.codingchili.core.metrics.MetricSettings;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static com.codingchili.core.configuration.CoreStrings.getUnsupportedDeployment;


/**
 * Implementation of the CoreContext, each context gets its own worker pool.
 */
public class SystemContext implements CoreContext {
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private final Set<String> deployments = ConcurrentHashMap.newKeySet();
    // shared with the contexts that are created from this context: they use the same vertx instance.
    private BlockingTasks blockingTasks = new BlockingTasks();
    private AtomicBoolean shuttingDown = new AtomicBoolean(false);
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

        if (context instanceof SystemContext parent) {
            this.blockingTasks = parent.blockingTasks;
            this.shuttingDown = parent.shuttingDown;
        }
    }

    private SystemContext(Vertx vertx) {
        this.vertx = vertx;
        initialize();
    }

    /**
     * Creates a clustered instance of a context.
     *
     * @return future completed with the context when the cluster has been joined.
     */
    public static Future<CoreContext> clustered() {
        return Vertx.clusteredVertx(Configurations.system().getOptions())
                .map(SystemContext::new);
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
        return track(vertx.deployVerticle(verticle, options(verticle)));
    }

    @Override
    public Future<String> handler(Supplier<CoreHandler> handler) {
        ListenerSettings settings = new ListenerSettings();
        return deployN(() -> new ClusterListener()
                .settings(settings)
                .handler(handler.get()));
    }

    @Override
    public Future<String> listener(Supplier<CoreListener> listener) {
        return deployN(listener::get);
    }

    @Override
    public Future<String> service(Supplier<CoreService> service) {
        return deployN(service::get);
    }

    private Future<String> deployN(Supplier<CoreDeployment> supplier) {
        // the first instance determines the instance count, reuse it as the first deployed instance.
        AtomicReference<CoreDeployment> first = new AtomicReference<>(supplier.get());
        DeploymentOptions options = options(first.get());

        return track(vertx.deployVerticle(() -> {
            CoreDeployment deployment = first.getAndSet(null);
            return new CoreVerticle((deployment == null) ? supplier.get() : deployment, this);
        }, options));
    }

    private DeploymentOptions options(Object deployable) {
        int instances = switch (deployable) {
            case DeploymentAware aware -> aware.instances();
            case CoreListener _ -> system().getListeners();
            case CoreService _ -> system().getServices();
            default -> system().getHandlers();
        };
        return new DeploymentOptions().setInstances(instances);
    }

    private Future<String> track(Future<String> deployment) {
        return deployment.onSuccess(deployments::add);
    }

    @Override
    public Future<Void> stop(String deploymentId) {
        deployments.remove(deploymentId);
        return vertx.undeploy(deploymentId);
    }

    @Override
    public Future<Void> stop() {
        List<Future<Void>> futures = deployments.stream()
                .map(vertx::undeploy)
                .toList();
        // prevent undeploying the same verticles twice.
        deployments.clear();
        return Future.all(futures).mapEmpty();
    }


    public Future<Void> blocking(Runnable blocking) {
        return blocking(() -> {
            blocking.run();
            return null;
        });
    }

    public Future<Void> blocking(Runnable blocking, boolean ordered) {
        return blocking(() -> {
            blocking.run();
            return null;
        }, ordered);
    }

    public <T> Future<T> blocking(Callable<T> blocking) {
        return tracked(() -> vertx.executeBlocking(blocking));
    }

    public <T> Future<T> blocking(Callable<T> blocking, boolean ordered) {
        return tracked(() -> vertx.executeBlocking(blocking, ordered));
    }

    /**
     * Keeps track of blocking tasks: closing vertx interrupts the tasks that are running, a graceful
     * shutdown waits for them before closing, see {@link #drain(long)}.
     */
    private <T> Future<T> tracked(Supplier<Future<T>> submit) {
        blockingTasks.begin();
        try {
            return submit.get().onComplete(done -> blockingTasks.end());
        } catch (RuntimeException e) {
            blockingTasks.end();
            throw e;
        }
    }

    /**
     * Waits for the blocking tasks that have been submitted through this context, or the contexts that
     * share its vertx instance, to complete.
     *
     * @param timeoutMS the maximum time to wait.
     * @return a future completed when all tasks have completed, or when the time is up.
     */
    Future<Void> drain(long timeoutMS) {
        return blockingTasks.await(vertx, timeoutMS);
    }

    @Override
    public int blockingTasks() {
        return blockingTasks.running();
    }

    /**
     * Marks the context as shutting down.
     *
     * @return false if it was already shutting down.
     */
    boolean beginShutdown() {
        return shuttingDown.compareAndSet(false, true);
    }

    @Override
    public boolean isShuttingDown() {
        return shuttingDown.get();
    }

    @Override
    public Logger logger(Class aClass) {
        return new RemoteLogger(this, aClass);
    }

    @Override
    public Future<Void> close() {
        initialized.set(false);
        shuttingDown.set(true);
        // the context is closed before the JVM exits: there is nothing left for the hook to shut down.
        ShutdownHook.unregister(this);
        // closing is best effort, failures are not propagated.
        return vertx.close().otherwiseEmpty();
    }

    @Override
    public Vertx vertx() {
        return vertx;
    }
}