package com.codingchili.core.listener;

import io.vertx.core.json.JsonObject;

import com.codingchili.core.listener.transport.Connection;
import com.codingchili.core.security.Token;

/**
 * Wraps a request to allow decorating request objects without re-initializing
 * the source request. Implement by handler-specific request classes.
 */
public interface RequestWrapper extends Request {

    /**
     * @return the wrapped request.
     */
    Request request();

    default void write(Object object) {
        request().write(object);
    }

    default void accept() {
        request().accept();
    }

    default void error(Throwable exception) {
        request().error(exception);
    }

    default String route() {
        return request().route();
    }

    default String target() {
        return request().target();
    }

    default Token token() {
        return request().token();
    }

    default Connection connection() {
        return request().connection();
    }

    default JsonObject data() {
        return request().data();
    }

    default int timeout() {
        return request().timeout();
    }

    default int size() {
        return request().size();
    }

    default int maxSize() {
        return request().maxSize();
    }
}
