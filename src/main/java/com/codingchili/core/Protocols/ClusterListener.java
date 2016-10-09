package com.codingchili.core.Protocols;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

/**
 * @author Robin Duda
 */
public class ClusterListener extends AbstractVerticle {
    private AbstractHandler handler;

    public ClusterListener(AbstractHandler handler) {
        this.handler = handler;
    }

    @Override
    public void start(Future<Void> start) {
        vertx.eventBus().consumer(handler.getAdddress()).handler(message -> {
            handler.process(new ClusterRequest(message));
        });

        handler.start(start);
    }

    @Override
    public void stop(Future<Void> stop) {
        handler.stop(stop);
    }
}
