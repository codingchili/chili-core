package com.codingchili.logging.model;

import com.codingchili.logging.configuration.LogContext;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.logging.*;
import com.codingchili.core.storage.JsonItem;

/**
 * @author Robin Duda
 *
 * Logs to an output storage.
 */
public class StorageLogger extends DefaultLogger implements JsonLogger {
    private final LogContext context;

    public StorageLogger(LogContext context) {
        this.context = context;
    }

    public Logger log(JsonObject data) {
        context.storage().put(new JsonItem(data), result -> {
            if (result.failed()) {
                throw new RuntimeException(result.cause());
            }
        });
        return this;
    }
}
