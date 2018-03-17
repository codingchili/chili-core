package com.codingchili.core.protocol;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.listener.Request;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * @author Robin Duda
 * <p>
 * Helper methods for creating response messages.
 * <p>
 * Response messages should always contain a reference to the request.
 */
public class Response {

    /**
     * Creates a response object given a response status.
     *
     * @param status the status to create the response from.
     * @return a JSON encoded response packed in a buffer.
     */
    public static JsonObject status(Request request, ResponseStatus status) {
        return addHeaders(request, new JsonObject()
                .put(PROTOCOL_STATUS, status));
    }

    /**
     * Creates a response object given a response status and a throwable.
     *
     * @param status the status to include in the response.
     * @param e      an exception that was the cause of an abnormal response status.
     * @return a JSON encoded response packed in a buffer.
     */
    public static JsonObject error(Request request, ResponseStatus status, Throwable e) {
        return addHeaders(request, new JsonObject()
                .put(PROTOCOL_STATUS, status)
                .put(PROTOCOL_MESSAGE, e.getMessage()));
    }

    /**
     * Creates a response message with a body and the accepted status.
     *
     * @param request the request to create a response for.
     * @param message the message to be sent as the response body.
     * @return a response message with request headers set.
     */
    public static JsonObject message(Request request, Object message) {
        return addHeaders(request, convert(message));
    }

    /**
     * Converts the given message into a serializable format used to
     * send messages back to clients. Use this method when the context
     * of a request is not available.
     *
     * @param message the message to be converted.
     * @return a sendable json object without headers.
     */
    public static JsonObject convert(Object message) {
        JsonObject json;
        if (message instanceof Buffer) {
            json = ((Buffer) message).toJsonObject();
        } else if (message instanceof JsonObject) {
            json = (JsonObject) message;
        } else {
            json = Serializer.json(message);
        }
        if (!json.containsKey(PROTOCOL_STATUS)) {
            json.put(PROTOCOL_STATUS, ResponseStatus.ACCEPTED);
        }
        return json;
    }

    private static JsonObject addHeaders(Request request, JsonObject message) {
        return message
                .put(PROTOCOL_TARGET, request.target())
                .put(PROTOCOL_ROUTE, request.route());
    }

}
