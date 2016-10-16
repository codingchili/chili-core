package com.codingchili.core.Routing.Controller.Transport;

import com.codingchili.core.Protocols.ClusterVerticle;
import com.codingchili.core.Routing.Controller.RouteHandler;
import com.codingchili.core.Routing.Configuration.ListenerSettings;
import io.vertx.core.Future;

/**
 * @author Robin Duda
 */
public class WebsocketListener extends ClusterVerticle {
    public WebsocketListener(RouteHandler provider, ListenerSettings listener) {

    }

    @Override
    public void start(Future<Void> start) {
        start.complete();
    }
}
