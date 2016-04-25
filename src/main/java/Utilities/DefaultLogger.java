package Utilities;

import Configuration.Config;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.time.Instant;

/**
 * Created by Robin on 2016-04-07.
 */
public class DefaultLogger implements Logger {
    private Vertx vertx;
    private String server;

    public DefaultLogger(Vertx vertx, String tag) {
        this.vertx = vertx;
        this.server = tag;

        vertx.createHttpClient().websocket(Config.Logging.PORT, Config.Logging.REMOTE, "/", handler -> {

            vertx.eventBus().consumer(Config.Address.LOGS, message -> {
                handler.writeFinalTextFrame(message.body().toString());
            });

            this.log(new JsonObject().put("event", "started"));
        });
    }

    @Override
    public void log(JsonObject json) {
        log(json
                .put("instance", Config.Logging.NAME)
                .put("server", server)
                .put("time", Instant.now().getEpochSecond())
                .encodePrettily());
    }

    private void log(String message) {
        vertx.eventBus().send(Config.Address.LOGS, message);
    }
}
