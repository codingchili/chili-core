package com.codingchili.core.Context;

import io.vertx.core.Future;

/**
 * @author Robin Duda
 */
public class Delay {
    private static CoreContext context;

    public static void initialize(CoreContext context) {
        Delay.context = context;
    }

    private static void future(Future future, long ms) {
        context.timer(ms, handler -> {
            future.complete();
        });
    }

    public static void forShutdown(Future<Void> future) {
        Delay.future(future, context.system().getShutdownLogTimeout());
    }
}
