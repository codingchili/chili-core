package Protocols;

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
    public void start(Future<Void> future) {
        vertx.eventBus().consumer(handler.getAdddress()).handler(message -> {
            handler.handle(new ClusterRequest(message));
        });

        future.complete();
    }
}
