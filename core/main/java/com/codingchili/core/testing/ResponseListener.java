package com.codingchili.core.testing;

import io.vertx.core.json.JsonObject;

import com.codingchili.core.protocol.ResponseStatus;

/**
 * Used together with the {@link RequestMock} to get the response body and code from a handler that the mocked
 * request was passed into.
 */

@FunctionalInterface
public interface ResponseListener {

    /**
     * Invoked when the processing handler returns a response.
     *
     * @param response the message body of the returned response.
     * @param status   the response status code from the handler.
     */
    void handle(JsonObject response, ResponseStatus status);
}

