package com.codingchili.core.Protocols;

/**
 * @author Robin Duda
 */

@FunctionalInterface
public interface RequestHandler<Request> {
    void handle(Request request);
}
