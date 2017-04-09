package com.codingchili.core.protocol;

import java.util.*;

import io.vertx.core.*;

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
        handler.context().bus().consumer(addressOf(handler)).handler(message -> {
            handler.process(new ClusterRequest(message));
        });
        handler.start(start);
    }

    private String addressOf(CoreHandler handler) {
        Optional<String> version = handler.context().identity().version();
        if (version.isPresent()) {
            return String.format("%s.%s", version.get(), handler.address());
        } else {
            return handler.address();
        }
    }

    @Override
    public void stop(Future<Void> stop) {
        handler.stop(stop);
    }
}
