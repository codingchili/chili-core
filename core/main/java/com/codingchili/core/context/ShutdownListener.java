package com.codingchili.core.context;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;

import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Allows a subscriber to receive shutdown events.
 */
public class ShutdownListener {
    private static Collection<Supplier<Future<Void>>> listeners = new LinkedList<>();

    /**
     * Adds a shutdown listener that is called when the application context is closed.
     *
     * @param runnable listener.
     */
    public synchronized static void subscribe(Supplier<Future<Void>> runnable) {
        listeners.add(runnable);
    }

    /**
     * Emits a shutdown event to all subscribers.
     */
    public synchronized static Future<Void> publish() {
        Future<Void> future = Future.future();
        CompositeFuture.all(listeners.stream()
                .map(Supplier::get)
                .collect(Collectors.toList()))
                .setHandler((done) -> {
                    if (done.succeeded()) {
                        future.complete();
                    } else {
                        future.fail(done.cause());
                    }
                });

        listeners.clear();
        return future;
    }
}
