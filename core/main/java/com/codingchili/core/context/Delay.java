package com.codingchili.core.context;

import io.vertx.core.Future;
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

    /**
     * Creates a future that completes after the specified ms.
     *
     * @param ms milliseconds to wait before completing the future.
     * @return a future completed after the delay.
     */
    public static Future<Void> forMS(long ms) {
        Promise<Void> promise = Promise.promise();
        context().timer(ms, handler -> promise.complete());
        return promise.future();
    }
}
