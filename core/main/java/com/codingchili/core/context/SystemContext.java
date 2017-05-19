package com.codingchili.core.context;

import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.MetricsService;

import java.lang.reflect.InvocationTargetException;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.configuration.system.SystemSettings;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.*;
import com.codingchili.core.listener.transport.ClusterListener;
import com.codingchili.core.logging.*;

import static com.codingchili.core.configuration.CoreStrings.ID_SYSTEM;


/**
 * @author Robin Duda
 *         <p>
 *         Implementation of the CoreContext, each context gets its own worker pool.
 */
public class SystemContext implements CoreContext {
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
        try {
            Object deployment = Class.forName(target).getConstructor().<Object>newInstance();

            if (deployment instanceof CoreHandler) {
                handler((CoreHandler) deployment, done);
            } else if (deployment instanceof CoreListener) {
                CoreListener listener = (CoreListener) deployment;
                listener.handler(new BusForwarder(this, target));
                listener.settings(ListenerSettings::new);
                listener(listener, done);
            } else if (deployment instanceof CoreService) {
                service((CoreService) deployment, done);
            } else if (deployment instanceof Verticle) {
                vertx.deployVerticle((Verticle) deployment, done);
            }

        } catch (NoSuchMethodException | InvocationTargetException | ClassNotFoundException |
                InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handler(CoreHandler handler, Handler<AsyncResult<String>> done) {
        vertx.deployVerticle(new CoreVerticle(new ClusterListener()
                .settings(ListenerSettings::new)
                .handler(handler), this), done);
    }

    @Override
    public void listener(CoreListener listener, Handler<AsyncResult<String>> done) {
        vertx.deployVerticle(new CoreVerticle(listener, this), done);
    }

    @Override
    public void service(CoreService service, Handler<AsyncResult<String>> done) {
        vertx.deployVerticle(new CoreVerticle(service, this), done);
    }

    @Override
    public void stop(String deploymentId) {
        vertx.undeploy(deploymentId);
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
