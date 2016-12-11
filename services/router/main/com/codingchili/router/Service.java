package com.codingchili.router;

import io.vertx.core.*;

import com.codingchili.core.protocol.ClusterNode;

import com.codingchili.router.configuration.ListenerSettings;
import com.codingchili.router.configuration.RouterContext;
import com.codingchili.router.controller.RouterHandler;
import com.codingchili.router.controller.transport.*;

/**
 * @author Robin Duda
 *         root game server, deploys realmName servers.
 */
public class Service extends ClusterNode {
    private RouterContext context;

    public Service(RouterContext context) {
        this.context = context;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);

        if (context == null) {
            this.context = new RouterContext(vertx);
        }
    }

    @Override
    public void start(Future<Void> start) {
        for (int i = 0; i < settings.getHandlers(); i++) {

            for (ListenerSettings listener : context.transports()) {
                RouterHandler<RouterContext> handler = new RouterHandler<>(context);

                switch (listener.getType()) {
                    case UDP:
                        context.deploy(new UdpListener(handler));
                        break;
                    case TCP:
                        context.deploy(new TcpListener(handler));
                        break;
                    case WEBSOCKET:
                        context.deploy(new WebsocketListener(handler));
                        break;
                    case REST:
                        context.deploy(new RestListener(handler));
                        break;
                }
            }
        }
        start.complete();
    }
}
