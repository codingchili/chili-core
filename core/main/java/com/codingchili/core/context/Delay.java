package com.codingchili.core.context;

import com.codingchili.core.context.exception.SystemNotInitializedException;
import io.vertx.core.Future;

/**
 * @author Robin Duda
 * <p>
 * Delays given futures to allow for cleanup or to implement backoff timers.
 */
public abstract class Delay {
    private static CoreContext context;

    static {
        StartupListener.subscibe(core -> {
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

    private static void future(Future future, long ms) {
        context().timer(ms, handler -> future.complete());
    }

    /**
     * Delays the given future for the specified ms.
     *
     * @param future the future to be delayed.
     * @param ms     milliseconds to wait before completing the future.
     */
    public static void forMS(Future<Void> future, long ms) {
        Delay.future(future, ms);
    }

    /**
     * Delays the given future with the shutdown-log timeout defined in system configuration.
     *
     * @param future the future to be delayed.
     */
    public static void forShutdown(Future<Void> future) {
        Delay.future(future, context().system().getShutdownLogTimeout());
    }
}
