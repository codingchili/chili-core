package com.codingchili.core.context;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.configuration.system.SystemSettings;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.*;
import com.codingchili.core.listener.transport.ClusterListener;
import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.logging.Level;
import com.codingchili.core.logging.Logger;

import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.MetricsService;

import static com.codingchili.core.configuration.CoreStrings.*;


/**
 * @author Robin Duda
 *         <p>
 *         Implementation of the CoreContext, each context gets its own worker pool.
 */
public class SystemContext implements CoreContext {
    private Map<String, List<String>> deployments = new HashMap<>();
    private WorkerExecutor executor;
    private ConsoleLogger console;
    protected Vertx vertx;

    protected SystemContext(CoreContext context) {
        this(context.vertx());
    }

    public SystemContext(Vertx vertx) {
        this.vertx = vertx;
        this.console = new ConsoleLogger(this);
        initialize();
    }

    private void initialize() {
        executor = vertx.createSharedWorkerExecutor("systemcontext", system().getWorkerPoolSize());

        MetricsService metrics = MetricsService.create(vertx);
        periodic(this::getMetricTimer, CoreStrings.LOG_METRICS, handler -> {
            if (system().isMetrics()) {
                JsonObject json = metrics.getMetricsSnapshot(vertx);
                this.onMetricsSnapshot(json);
            }
        });
    }

    private int getMetricTimer() {
        return system().getMetricRate();
    }

    protected void onMetricsSnapshot(JsonObject json) {
        if (json != null) {
            console.log(json);
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
                logger().onTimerSourceChanged(name, initial, timeout.getMS());
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
    public void deploy(String target, Handler<AsyncResult<String>> done) {
        Supplier<Object> deployment = () -> {
            try {
                return Class.forName(target).<Object>newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new CoreRuntimeException(e.getMessage());
            }
        };

        if (deployment.get() instanceof CoreHandler) {
            handler(() -> (CoreHandler) deployment.get(), done);
        } else if (deployment.get() instanceof CoreListener) {
            listener(() -> {
                CoreListener listener = (CoreListener) deployment.get();
                listener.handler(new BusForwarder(this, target));
                listener.settings(ListenerSettings::new);
                return listener;
            }, done);
        } else if (deployment.get() instanceof CoreService) {
            service(() -> (CoreService) deployment.get(), done);
        } else if (deployment.get() instanceof Verticle) {
            deployN(target, done);
        } else {
            done.handle(Future.failedFuture(getUnsupportedDeployment(target)));
        }
    }

    private void deployN(String verticle, Handler<AsyncResult<String>> done) {
        vertx.deployVerticle(verticle,
                new DeploymentOptions().setInstances(system().getHandlers()), done);
    }

    @Override
    public void handler(Supplier<CoreHandler> handler, Handler<AsyncResult<String>> done) {
        deployN(() -> new ClusterListener()
                .settings(ListenerSettings::new)
                .handler(handler.get()), done);
    }

    @Override
    public void listener(Supplier<CoreListener> listener, Handler<AsyncResult<String>> done) {
        deployN(listener::get, done);
    }

    @Override
    public void service(Supplier<CoreService> service, Handler<AsyncResult<String>> done) {
        deployN(service::get, done);
    }

    private void deployN(Supplier<CoreDeployment> supplier, Handler<AsyncResult<String>> done) {
        CoreDeployment deployment = supplier.get();
        CountDownLatch latch = new CountDownLatch(getHandlerCount(deployment));
        String deploymentId = UUID.randomUUID().toString();
        List<String> completed = new ArrayList<>();
        int toDeploy = getHandlerCount(deployment);

        for (int i = 0; i < getHandlerCount(deployment); i++) {
            vertx.deployVerticle(new CoreVerticle(deployment, this), deployed -> {
                if (deployed.succeeded()) {
                    completed.add(deployed.result());
                    latch.countDown();
                    if (latch.getCount() == 0) {
                        done.handle(Future.succeededFuture(deploymentId));
                        deployments.put(deploymentId, completed);
                    }
                } else {
                    done.handle(Future.failedFuture(deployed.cause()));
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
    public String name() {
        return getClass().getSimpleName();
    }

    @Override
    public Logger logger() {
        if (system().isConsoleLogging()) {
            return console.setEnabled(true);
        } else {
            return console.setEnabled(false);
        }
    }

    @Override
    public Vertx vertx() {
        return vertx;
    }

    @Override
    public String node() {
        return ID_SYSTEM;
    }

    protected JsonObject event(String event) {
        return event(event, Level.INFO);
    }

    protected JsonObject event(String event, Level level) {
        return logger().event(event, level);
    }
}
