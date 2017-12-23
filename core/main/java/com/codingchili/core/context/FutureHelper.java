package com.codingchili.core.context;

import io.vertx.core.Future;

/**
 * @author Robin Duda
 *
 * Helper method to simplify working with handlers and asyncresult.
 */
public abstract class FutureHelper {

    /**
     * @param e the cause of the failure.
     * @param <T> inferred future type
     * @return a failed future with the given cause.
     */
    public static <T> Future<T> error(Throwable e) {
        return Future.failedFuture(e);
    }

    /**
     * @param value the value that is the result
     * @param <T> inferred future type
     * @return a succeeded future with the given value.
     */
    public static <T> Future<T> result(T value) {
        return Future.succeededFuture(value);
    }

    /**
     * @param <T> inferred future type
     * @return a succeeded future with an empty result.
     */
    public static <T> Future<T> result() {
        return Future.succeededFuture();
    }

    /**
     * @param typed future of any type
     * @param <T> inferred future type
     * @return a generic future with a handler set.
     */
    public static <T> Future<T> generic(Future<?> typed) {
        Future<T> future = Future.future();

        future.setHandler(done -> {
            if (done.succeeded()) {
                typed.complete();
            } else {
                typed.fail(done.cause());
            }
        });

        return future;
    }
}
