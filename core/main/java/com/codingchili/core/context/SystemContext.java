package com.codingchili.core.context;

import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.MetricsService;

import java.util.UUID;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.configuration.system.SystemSettings;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.*;
import com.codingchili.core.protocol.AbstractHandler;
import com.codingchili.core.protocol.ClusterListener;
import com.codingchili.core.security.RemoteIdentity;

import static com.codingchili.core.configuration.CoreStrings.ID_SYSTEM;

/**
 * @author Robin Duda
 *
 * Implementation of the CoreContext, each context gets its own worker pool.
 */
public class SystemContext implements CoreContext {
    private final ConsoleLogger console;
    private WorkerExecutor executor;
    protected Vertx vertx;

    SystemContext(CoreContext context) {
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
                onMetricsSnapshot(json);
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
                periodic(timeout, name, handler);

                console().onTimerSourceChanged(name, initial, timeout.getMS());
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
    public void deploy(AbstractHandler handler) {
        deploy(ClusterListener.with(handler), result -> {
            if (result.failed()) {
                throw new RuntimeException(result.cause());
            }
        });
    }

    @Override
    public void deploy(Verticle verticle) {
        vertx.deployVerticle(verticle, result -> {
            if (result.failed()) {
                throw new RuntimeException(result.cause());
            }
        });
    }

    @Override
    public void deploy(String verticle, Handler<AsyncResult<String>> result) {
        vertx.deployVerticle(verticle, result);
    }

    @Override
    public void deploy(AbstractHandler handler, Handler<AsyncResult<String>> result) {
        vertx.deployVerticle(ClusterListener.with(handler), result);
    }

    @Override
    public void deploy(Verticle verticle, Handler<AsyncResult<String>> result) {
        vertx.deployVerticle(verticle, result);
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
    public String handler() {
        return getClass().getSimpleName();
    }

    @Override
    public Logger console() {
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
    public RemoteIdentity identity() {
        return new RemoteIdentity(ID_SYSTEM, CoreStrings.NODE_LOCAL);
    }


    protected JsonObject event(String event) {
        return event(event, Level.INFO);
    }

    protected JsonObject event(String event, Level level) {
        return console().event(event, level);
    }
}
