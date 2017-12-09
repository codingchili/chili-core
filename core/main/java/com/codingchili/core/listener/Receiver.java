package com.codingchili.core.listener;

/**
 * @author Robin Duda
 *
 * A handler to handle things.
 */
public interface Receiver<T> {

    /**
     * Handles an incoming request without exception handling.
     *
     * @param request the request to be handled.
     */
    void handle(T request);
}
