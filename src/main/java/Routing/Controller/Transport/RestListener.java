package Routing.Controller.Transport;

import Routing.Configuration.RouteProvider;
import Routing.Model.ListenerSettings;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;

/**
 * @author Robin Duda
 */
public class RestListener extends AbstractVerticle {
    private Vertx vertx;
    private ListenerSettings listener;

    public RestListener(RouteProvider provider, ListenerSettings listener) {
        this.vertx = provider.getVertx();
        this.listener = listener;
    }

    @Override
    public void start(Future<Void> start) {
        vertx.createHttpServer().requestHandler(handler -> {

            handler.bodyHandler(body -> {
                handler.response().write(Buffer.buffer("{the data..}"));
            });

        }).listen(listener.getPort());
    }
}
