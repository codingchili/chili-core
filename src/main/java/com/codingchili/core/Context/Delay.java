package com.codingchili.core.Context;

import io.vertx.core.Future;

/**
 * @author Robin Duda
 *
 * Delays given futures to allow for cleanup or to implement backoff timers.
 */
public class Delay {
    private static CoreContext context;

    /**
     * Initializes the delay system with a core context.
     * @param context containing timer implementation to use when delaying.
     */
    public static void initialize(CoreContext context) {
        Delay.context = context;
    }

    private static void future(Future future, long ms) {
        context.timer(ms, handler -> {
            future.complete();
        });
    }

    /**
     * Delays the given future with the shutdown-log timeout defined in system configuration.
     * @param future the future to be delayed.
     */
    public static void forShutdown(Future<Void> future) {
        Delay.future(future, context.system().getShutdownLogTimeout());
    }
}
