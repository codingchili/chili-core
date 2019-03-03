package com.codingchili.core.protocol;

/**
 * Functional interface used for routes in a handler.
 */

@FunctionalInterface
public interface RequestHandler<Request> {
    void submit(Request request);
}
