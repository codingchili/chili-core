package Protocols;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

/**
 * @author Robin Duda
 */
public class ClusterListener extends AbstractVerticle {
    private HandlerProvider handler;

    public ClusterListener(HandlerProvider handler) {
        this.handler = handler;
    }

    @Override
    public void start(Future<Void> future) {
        vertx.eventBus().consumer(handler.getAdddress()).handler(message -> {
            handler.process(new ClusterMessage(message));
        });

        future.complete();
    }
}
