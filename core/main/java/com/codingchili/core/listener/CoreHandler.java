package com.codingchili.core.listener;

/**
 * @author Robin Duda
 *         <p>
 *         A simplified handler that may be deployed directly.
 */
public interface CoreHandler extends CoreLifecycle {

    /**
     * Handles an incoming request without exception handling.
     *
     * @param request the request to be handled.
     */
    void handle(Request request);

    /**
     * @return the address of the handler.
     */
    String address();
}