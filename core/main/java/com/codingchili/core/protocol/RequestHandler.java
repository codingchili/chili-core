package com.codingchili.core.protocol;

/**
 * @author Robin Duda
 * <p>
 * Functional interface used for routes in a handler.
 */

@FunctionalInterface
public interface RequestHandler<Request> {
    void submit(Request request);
}
