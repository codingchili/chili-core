package com.codingchili.core.Logging;

import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 */
public interface JsonLogger {
    JsonLogger log(JsonObject data);
}
