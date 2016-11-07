package com.codingchili.core.Protocol;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

/**
 * @author Robin Duda
 */
public class ClusterListener extends AbstractVerticle {
    private final AbstractHandler handler;

    public ClusterListener(AbstractHandler handler) {
        this.handler = handler;
    }

    @Override
    public void start(Future<Void> start) {
        handler.context.bus().consumer(handler.getAddress()).handler(message -> {
            handler.process(new ClusterRequest(message));
        });

        handler.start(start);
    }

    @Override
    public void stop(Future<Void> stop) {
        handler.stop(stop);
    }
}
