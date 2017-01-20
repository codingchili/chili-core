package com.codingchili.core.protocol;

/**
 * @author Robin Duda
 *
 * Protocol exception handler.
 */
@FunctionalInterface
interface ExceptionHandler {
    void handle(Request request, Throwable exception);
}
