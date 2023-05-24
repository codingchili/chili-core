package com.codingchili.core.protocol;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * Helper methods for creating response messages.
 * <p>
 * Response messages should always contain a reference to the request.
 */
public abstract class Response {

    /**
     * Creates a response object given a response status.
     *
     * @param target the target header to set for the response.
     * @param route  the route header to set for the response.
     * @param status the status to create the response from.
     * @return a JSON encoded response packed in a buffer.
     */
    public static JsonObject status(String target, String route, ResponseStatus status) {
        return addHeaders(target, route, new JsonObject()
                .put(PROTOCOL_STATUS, status));
    }

    /**
     * Creates a response object given a response status and a throwable.
     *
     * @param target the target header to set for the response.
     * @param route  the route header to set for the response.
     * @param status the status to include in the response.
     * @param e      an exception that was the cause of an abnormal response status.
     * @return a JSON encoded response packed in a buffer.
     */
    public static JsonObject error(String target, String route, ResponseStatus status, Throwable e) {
        return addHeaders(target, route, new JsonObject()
                .put(PROTOCOL_STATUS, status)
                .put(PROTOCOL_MESSAGE, e.getMessage()));
    }


    public static Buffer buffer(Object object) {
        return Serializer.buffer(object);
    }

    public static JsonObject json(Object object) {
        return Serializer.json(object);
    }

    /**
     * Adds response headers for a request.
     *
     * @param target  the target header to set for the response.
     * @param route   the route header to set for the response.
     * @param message the response json object.
     * @return a json object with headers from the request added.
     */
    public static JsonObject addHeaders(String target, String route, JsonObject message) {
        if (!message.containsKey(PROTOCOL_STATUS)) {
            message.put(PROTOCOL_STATUS, ResponseStatus.ACCEPTED);
        }
        if (target != null && !message.containsKey(PROTOCOL_TARGET)) {
            message.put(PROTOCOL_TARGET, target);
        }
        if (route != null && !message.containsKey(PROTOCOL_ROUTE)) {
            message.put(PROTOCOL_ROUTE, route);
        }
        return message;
    }

}
