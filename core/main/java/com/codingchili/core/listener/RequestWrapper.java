package com.codingchili.core.listener;

import com.codingchili.core.security.Token;
import io.vertx.core.json.JsonObject;

/**
 * Wraps a request to allow decorating request objects without re-initializing
 * the source request. Extend with handler-specific request classes.
 */
public class RequestWrapper implements Request {
    protected Request request;

    public RequestWrapper(Request request) {
        this.request = request;
    }

    @Override
    public void init() {
        request.init();
    }

    @Override
    public void write(Object object) {
        request.write(object);
    }

    @Override
    public void accept() {
        request.accept();
    }

    @Override
    public void error(Throwable exception) {
        request.error(exception);
    }

    @Override
    public String route() {
        return request.route();
    }

    @Override
    public String target() {
        return request.target();
    }

    @Override
    public Token token() {
        return request.token();
    }

    @Override
    public JsonObject data() {
        return request.data();
    }

    @Override
    public int timeout() {
        return request.timeout();
    }

    @Override
    public int size() {
        return request.size();
    }

    @Override
    public int maxSize() {
        return request.maxSize();
    }
}
