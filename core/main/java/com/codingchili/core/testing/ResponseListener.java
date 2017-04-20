package com.codingchili.core.testing;

import com.codingchili.core.protocol.ResponseStatus;

import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 */

@FunctionalInterface
public interface ResponseListener {
    void handle(JsonObject response, ResponseStatus status);
}

