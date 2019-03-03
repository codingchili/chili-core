package com.codingchili.core.protocol;

import com.codingchili.core.listener.Request;

/**
 * Protocol exception handler.
 */
@FunctionalInterface
public interface ExceptionHandler {
    void handle(Request request, Throwable exception);
}
