package com.codingchili.services.Router;

import io.vertx.core.*;

import com.codingchili.core.Protocol.ClusterNode;

import com.codingchili.services.Router.Configuration.ListenerSettings;
import com.codingchili.services.Router.Configuration.RouterContext;
import com.codingchili.services.Router.Controller.RouterHandler;
import com.codingchili.services.Router.Controller.Transport.*;

/**
 * @author Robin Duda
 *         root game server, deploys realmName servers.
 */
public class Service extends ClusterNode {

    @Override
    public void start(Future<Void> start) {
        RouterContext context = new RouterContext(vertx);

        for (int i = 0; i < settings.getHandlers(); i++) {

            for (ListenerSettings listener : context.transports()) {
                switch (listener.getType()) {
                    case UDP:
                        context.deploy(new UdpListener(new RouterHandler<>(context), listener));
                        break;
                    case TCP:
                        context.deploy(new TcpListener(new RouterHandler<>(context), listener));
                        break;
                    case WEBSOCKET:
                        context.deploy(new WebsocketListener(new RouterHandler<>(context), listener));
                        break;
                    case REST:
                        context.deploy(new RestListener(new RouterHandler<>(context)));
                        break;
                }
            }
        }
        start.complete();
    }
}
