package com.codingchili.core.Routing.Controller.Transport;

import com.codingchili.core.Protocols.ClusterVerticle;
import com.codingchili.core.Routing.Configuration.ListenerSettings;
import com.codingchili.core.Routing.Controller.RouteHandler;
import io.vertx.core.Future;

/**
 * @author Robin Duda
 */
public class TcpListener extends ClusterVerticle {
    public TcpListener(RouteHandler handler, ListenerSettings listener) {

    }

    @Override
    public void start(Future<Void> start) {
        start.complete();
    }
}
