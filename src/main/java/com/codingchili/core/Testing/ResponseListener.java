package com.codingchili.core.Testing;

import io.vertx.core.json.JsonObject;

import com.codingchili.core.Protocol.ResponseStatus;

/**
 * @author Robin Duda
 */

@FunctionalInterface
public interface ResponseListener {
    void handle(JsonObject response, ResponseStatus status);
}

