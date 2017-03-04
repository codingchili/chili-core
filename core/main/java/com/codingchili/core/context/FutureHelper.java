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
}
