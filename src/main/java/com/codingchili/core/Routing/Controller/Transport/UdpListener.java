package com.codingchili.core.Routing.Controller.Transport;

import com.codingchili.core.Protocols.ClusterVerticle;
import com.codingchili.core.Routing.Controller.RoutingHandler;
import com.codingchili.core.Routing.Model.ListenerSettings;
import io.vertx.core.Future;

/**
 * @author Robin Duda
 */
public class UdpListener extends ClusterVerticle {
    public UdpListener(RoutingHandler handler, ListenerSettings listener) {

    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {

    }
}
