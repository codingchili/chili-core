package com.codingchili.logging.model;

import com.codingchili.core.logging.DefaultLogger;
import com.codingchili.core.logging.JsonLogger;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.storage.JsonItem;
import com.codingchili.logging.configuration.LogContext;
import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 * <p>
 * Logs to an output storage.
 */
public class StorageLogger extends DefaultLogger implements JsonLogger {
    private final LogContext context;

    public StorageLogger(LogContext context, Class aClass) {
        super(context, aClass);
        this.context = context;
    }

    public Logger log(JsonObject data) {
        JsonItem item = new JsonItem();
        item.mergeIn(data);

        context.storage().put(item, result -> {
            if (result.failed()) {
                throw new RuntimeException(result.cause());
            }
        });
        return this;
    }
}
