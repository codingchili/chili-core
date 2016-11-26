package com.codingchili.core.testing;

import io.vertx.core.json.JsonObject;

import com.codingchili.core.protocol.ResponseStatus;

/**
 * @author Robin Duda
 */

@FunctionalInterface
public interface ResponseListener {
    void handle(JsonObject response, ResponseStatus status);
}

