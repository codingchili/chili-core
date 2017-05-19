package com.codingchili.logging.model;

import com.codingchili.core.logging.DefaultLogger;
import com.codingchili.core.logging.JsonLogger;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.storage.JsonItem;
import com.codingchili.logging.configuration.LogContext;

import io.vertx.core.json.JsonObject;

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
