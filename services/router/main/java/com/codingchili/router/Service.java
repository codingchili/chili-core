package com.codingchili.router;

import com.codingchili.router.configuration.ListenerSettings;
import com.codingchili.router.configuration.RouterContext;
import com.codingchili.router.controller.RouterHandler;
import com.codingchili.router.controller.transport.*;
import io.vertx.core.*;

import java.util.ArrayList;
import java.util.List;

import com.codingchili.core.protocol.ClusterNode;

import static io.vertx.core.CompositeFuture.all;

/**
 * @author Robin Duda
 *         root game server, deploys realmName servers.
 */
public class Service extends ClusterNode {
    private RouterContext context;

    public Service() {
    }

    public Service(RouterContext context) {
        this.context = context;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);

        if (this.context == null) {
            this.context = new RouterContext(vertx);
        }
    }

    @Override
    public void start(Future<Void> start) {
        List<Future> deployments = new ArrayList<>();

        for (ListenerSettings listener : context.transports()) {
            RouterHandler handler = new RouterHandler(context);

            for (int i = 0; i < settings.getHandlers(); i++) {
                Future<String> future = Future.future();
                deployments.add(future);
                boolean singleHandlerOnly = false;

                switch (listener.getType()) {
                    case UDP:
                        context.deploy(new UdpListener(handler), future);
                        singleHandlerOnly = true;
                        break;
                    case TCP:
                        context.deploy(new TcpListener(handler), future);
                        break;
                    case WEBSOCKET:
                        context.deploy(new WebsocketListener(handler), future);
                        break;
                    case REST:
                        context.deploy(new RestListener(handler), future);
                        break;
                }
                if (singleHandlerOnly) {
                    break;
                }
            }
        }
        all(deployments).setHandler(done -> start.complete());
    }
}
