package com.codingchili.core.context;

import io.vertx.core.Future;
import io.vertx.ext.unit.Async;

import com.codingchili.core.context.exception.SystemNotInitializedException;

/**
 * @author Robin Duda
 *
 * Delays given futures to allow for cleanup or to implement backoff timers.
 */
public abstract class Delay {
    private static CoreContext context;

    /**
     * Initializes the delay system with a core context.
     * @param context containing timer implementation to use when delaying.
     */
    public static void initialize(CoreContext context) {
        Delay.context = context;
    }

    private static CoreContext context() {
        if (context == null) {
            throw new SystemNotInitializedException(Delay.class);
        } else {
            return context;
        }
    }

    private static void future(Future future, long ms) {
        context().timer(ms, handler -> future.complete());
    }

    /**
     * Delays the given async for the specified ms.
     * @param async the async to be delayed.
     * @param ms milliseconds to wait before completing the async.
     */
    public static void forMS(Async async, long ms) {
        context().timer(ms, handler -> async.complete());
    }

    /**
     * Delays the given future for the specified ms.
     * @param future the future to be delayed.
     * @param ms milliseconds to wait before completing the future.
     */
    public static void forMS(Future<Void> future, long ms) {
        Delay.future(future, ms);
    }

    /**
     * Delays the given future with the shutdown-log timeout defined in system configuration.
     * @param future the future to be delayed.
     */
    public static void forShutdown(Future<Void> future) {
        Delay.future(future, context().system().getShutdownLogTimeout());
    }
}
