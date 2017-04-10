package com.codingchili.core.protocol;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

/**
 * @author Robin Duda
 *         <p>
 *         Listens for requests addressed to the attached handler and forwards
 *         the requests to it.
 */
public class ClusterListener extends AbstractVerticle {
    private final CoreHandler handler;

    /**
     * Creates a new ClusterListener with the given handler.
     *
     * @param handler the handler to be used for received messages.
     * @return a ClusterListener instance with the handler attached.
     */
    public static ClusterListener with(CoreHandler handler) {
        return new ClusterListener(handler);
    }

    /**
     * @param handler a handler that is to receive messages from the cluster
     *                where the target address matches the handler address.
     */
    private ClusterListener(CoreHandler handler) {
        this.handler = handler;
    }

    @Override
    public void start(Future<Void> start) {
        handler.context().bus().consumer(handler.address()).handler(message -> {
            handler.process(new ClusterRequest(message));
        });
        handler.start(start);
    }

    @Override
    public void stop(Future<Void> stop) {
        handler.stop(stop);
    }
}
