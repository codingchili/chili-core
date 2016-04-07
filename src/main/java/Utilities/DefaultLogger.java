package Utilities;

import Configuration.Configuration.*;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by Robin on 2016-04-07.
 */
public class DefaultLogger implements Logger {
    private Vertx vertx;
    private String tag;

    public DefaultLogger(Vertx vertx, String tag) {
        this.vertx = vertx;
        this.tag = "[" + tag + "] ";
        this.log("Starting..");
    }

    @Override
    public void log(String message) {
        vertx.eventBus().send(Address.LOGS, tag + message);
    }

    public void log(JsonObject json) {
        log(json.encodePrettily());
    }

    @Override
    public void configuration(JsonObject json) {
        log("Using configuration\r\n" + json.encodePrettily());
    }

}
