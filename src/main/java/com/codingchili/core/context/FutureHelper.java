package com.codingchili.core.context;

import io.vertx.core.Future;

/**
 * @author Robin Duda
 */
public abstract class FutureHelper {

    public static <T> Future<T> failed(Throwable e) {
        return Future.failedFuture(e);
    }

    public static <T> Future<T> succeeded(T value) {
        return Future.succeededFuture(value);
    }

    public static <T> Future<T> succeeded() {
        return Future.succeededFuture();
    }
}
