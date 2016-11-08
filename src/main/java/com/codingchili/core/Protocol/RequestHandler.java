package com.codingchili.core.Protocol;

/**
 * @author Robin Duda
 */

@FunctionalInterface
public interface RequestHandler<Request> {
    void handle(Request request);
}
