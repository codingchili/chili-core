package Routing.Controller.Transport;

import Protocols.ClusterVerticle;
import Routing.Configuration.RouteProvider;
import Routing.Controller.RoutingHandler;
import Routing.Model.ListenerSettings;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

/**
 * @author Robin Duda
 */
public class WebsocketListener extends ClusterVerticle {
    public WebsocketListener(RoutingHandler provider, ListenerSettings listener) {

    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {

    }
}
