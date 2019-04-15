package com.codingchili.core.context;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Allows a subscriber to receive shutdown events.
 */
public class ShutdownListener {
    private static Collection<Function<Optional<CoreContext>, Future<Void>>> listeners = new LinkedList<>();

    /**
     * Adds a shutdown listener that is called right before a context is closed.
     *
     * @param runnable listener.
     */
    public synchronized static void subscribe(Function<Optional<CoreContext>, Future<Void>> runnable) {
        listeners.add(runnable);
    }

    /**
     * Unsubscribe from all shutdown events.
     *
     * @param listener the listener to be removed.
     */
    public synchronized static void unsubscribe(Function<Optional<CoreContext>, Future<Void>> listener) {
        listeners.remove(listener);
    }

    /**
     * Emits a shutdown event to all subscribers.
     *
     * @param core the context that was shut down.
     */
    public synchronized static Future<Void> publish(CoreContext core) {
        Future<Void> future = Future.future();
        CompositeFuture.all(listeners.stream()
                .map(listener -> listener.apply(Optional.ofNullable(core)))
                .collect(Collectors.toList()))
                .setHandler((done) -> {
                    if (done.succeeded()) {
                        future.complete();
                    } else {
                        future.fail(done.cause());
                    }
                });
        return future;
    }
}
