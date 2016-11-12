package com.codingchili.core.Context;

import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.MetricsService;

import com.codingchili.core.Configuration.Strings;
import com.codingchili.core.Configuration.System.SystemSettings;
import com.codingchili.core.Files.Configurations;
import com.codingchili.core.Logging.ConsoleLogger;
import com.codingchili.core.Logging.Logger;
import com.codingchili.core.Protocol.AbstractHandler;
import com.codingchili.core.Protocol.ClusterListener;
import com.codingchili.core.Security.RemoteIdentity;

/**
 * @author Robin Duda
 *
 * Implementation of the CoreContext.
 */
public abstract class SystemContext implements CoreContext {
    private final ConsoleLogger console;
    protected Vertx vertx;

    SystemContext(CoreContext context) {
        this.vertx = context.vertx();
        this.console = new ConsoleLogger(this);
    }

    public SystemContext(Vertx vertx) {
        this.vertx = vertx;
        this.console = new ConsoleLogger(this);

        initialize();
    }

    private void initialize() {
        MetricsService metrics = MetricsService.create(vertx);

        periodic(this::getMetricTimer, Strings.LOG_METRICS,handler -> {
            if (system().isMetrics()) {
                JsonObject json = metrics.getMetricsSnapshot(vertx);
                onMetricsSnapshot(json);
            }
        });
    }

    private int getMetricTimer() {
        return system().getMetricRate();
    }

    void onMetricsSnapshot(JsonObject json) {
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
        deploy(new ClusterListener(handler), result -> {
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
        vertx.deployVerticle(new ClusterListener(handler), result);
    }

    @Override
    public void deploy(Verticle verticle, Handler<AsyncResult<String>> result) {
        vertx.deployVerticle(verticle, result);
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
    public abstract RemoteIdentity identity();
}
