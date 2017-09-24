package com.codingchili.core.context;

import io.vertx.core.Future;

/**
 * @author Robin Duda
 */
public abstract class FutureHelper {

    public static <T> Future<T> error(Throwable e) {
        return Future.failedFuture(e);
    }

    public static <T> Future<T> result(T value) {
        return Future.succeededFuture(value);
    }

    public static <T> Future<T> result() {
        return Future.succeededFuture();
    }

    public static <T> Future<T> generic(Future<?> typed) {
        Future<T> future = Future.future();

        future.setHandler(done -> {
            if (done.succeeded()) {
                typed.succeeded();
            } else {
                typed.fail(done.cause());
            }
        });

        return future;
    }
}
