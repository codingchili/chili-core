package com.codingchili.core.logging;

import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 *
 * Interface for a logger that logs Json objects.
 */
public interface JsonLogger {
    /**
     * @param data the data to be logged.
     */
    JsonLogger log(JsonObject data);
}
