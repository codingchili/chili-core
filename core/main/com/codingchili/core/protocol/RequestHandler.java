package com.codingchili.core.protocol;

/**
 * @author Robin Duda
 */

@FunctionalInterface
public interface RequestHandler<Request> {
    void handle(Request request);
}
