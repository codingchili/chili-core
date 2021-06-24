package com.codingchili.core.context;

import io.vertx.core.Future;
import io.vertx.core.Promise;

/**
 * Helper method to simplify working with handlers and asyncresult.
 */
public abstract class FutureHelper {

    private FutureHelper() {}

    /**
     * Creates a failed future from a throwable.
     *
     * @param e   the cause of the failure.
     * @param <T> inferred future type
     * @return a failed future with the given cause.
     */
    public static <T> Future<T> error(Throwable e) {
        return Future.failedFuture(e);
    }

    /**
     * Creates a succeeded future from the given value.
     *
     * @param value the value that is the result
     * @param <T>   inferred future type
     * @return a succeeded future with the given value.
     */
    public static <T> Future<T> result(T value) {
        return Future.succeededFuture(value);
    }

    /**
     * Creates a succeeded future with an empty result.
     *
     * @param <T> inferred future type
     * @return a succeeded future with an empty result.
     */
    public static <T> Future<T> result() {
        return Future.succeededFuture();
    }

    /**
     * Converts the given untyped feature into an inferred type by wrapping.
     *
     * @param typed future of any type
     * @param <T>   inferred future type
     * @return a generic future with a handler set.
     */
    public static <T> Promise<T> untyped(Promise<?> typed) {
        Promise<T> promise = Promise.promise();

        promise.future().onComplete(done -> {
            if (done.succeeded()) {
                typed.complete();
            } else {
                typed.fail(done.cause());
            }
        });

        return promise;
    }
}
