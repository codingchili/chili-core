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

    /**
     * @param message the message to be converted to a buffer.
     * @return a message buffer.
     * @see #buffer(Object)
     */
    public static Buffer buffer(Object message) {
        return buffer(null, null, message);
    }

    /**
     * Converts the given message into a serializable format used to
     * send messages back to clients. Use this method when the context
     * of a request is not available.
     *
     * @param message the message to be converted.
     * @param target  the target header to set for the response.
     * @param route   the route header to set for the response.
     * @return a sendable json object without headers.
     */
    public static Buffer buffer(String target, String route, Object message) {
        Buffer buffer;

        if (message instanceof Buffer) {
            buffer = ((Buffer) message);
        } else if (message instanceof JsonObject) {
            buffer = addHeaders(target, route, (JsonObject) message).toBuffer();
        } else {
            buffer = addHeaders(target, route, Serializer.json(message)).toBuffer();
        }
        return buffer;
    }

    /**
     * @param message an object to be converted to a json message.
     * @return a json message with headers set.
     * @see #json(Object)
     */
    public static JsonObject json(Object message) {
        return json(null, null, message);
    }

    /**
     * Creates a json response message.
     *
     * @param target  the target header to set for the response.
     * @param route   the route header to set for the response.
     * @param message the message to add request headers to.
     * @return a json object.
     */
    public static JsonObject json(String target, String route, Object message) {
        JsonObject json;

        if (message instanceof JsonObject) {
            json = (JsonObject) message;
        } else if (message instanceof Buffer) {
            json = ((Buffer) message).toJsonObject();
        } else {
            json = Serializer.json(message);
        }
        return addHeaders(target, route, json);
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
