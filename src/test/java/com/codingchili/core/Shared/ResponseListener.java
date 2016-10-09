package com.codingchili.core.Shared;

import com.codingchili.core.Protocols.ResponseStatus;
import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 */

@FunctionalInterface
public interface ResponseListener {
    void handle(JsonObject response, ResponseStatus status);
}

