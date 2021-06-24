package com.codingchili.core.context;

import io.vertx.core.Promise;

import com.codingchili.core.context.exception.SystemNotInitializedException;

/**
 * Delays given futures to allow for cleanup or to implement backoff timers.
 */
public abstract class Delay {
    private static CoreContext context;

    static {
        StartupListener.subscribe(core -> {
            Delay.context = core;
        });
    }

    private static CoreContext context() {
        if (context == null) {
            throw new SystemNotInitializedException(Delay.class);
        } else {
            return context;
        }
    }

    private static void future(Promise<Void> promise, long ms) {
        context().timer(ms, handler -> promise.complete());
    }

    /**
     * Delays the given future for the specified ms.
     *
     * @param future the future to be delayed.
     * @param ms     milliseconds to wait before completing the future.
     */
    public static void forMS(Promise<Void> future, long ms) {
        Delay.future(future, ms);
    }
}
