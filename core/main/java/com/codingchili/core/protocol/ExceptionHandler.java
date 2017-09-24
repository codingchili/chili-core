package com.codingchili.core.protocol;

import com.codingchili.core.listener.Request;

/**
 * @author Robin Duda
 * <p>
 * Protocol exception handler.
 */
@FunctionalInterface
public interface ExceptionHandler {
    void handle(Request request, Throwable exception);
}
