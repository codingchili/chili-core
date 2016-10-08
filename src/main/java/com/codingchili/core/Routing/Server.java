package com.codingchili.core.Routing;

import com.codingchili.core.Configuration.FileConfiguration;
import com.codingchili.core.Logging.Model.DefaultLogger;
import com.codingchili.core.Protocols.ClusterVerticle;
import com.codingchili.core.Routing.Configuration.RouteProvider;
import com.codingchili.core.Routing.Configuration.RoutingSettings;
import com.codingchili.core.Routing.Controller.RouteHandler;
import com.codingchili.core.Routing.Controller.Transport.RestListener;
import com.codingchili.core.Routing.Controller.Transport.TcpListener;
import com.codingchili.core.Routing.Controller.Transport.UdpListener;
import com.codingchili.core.Routing.Controller.Transport.WebsocketListener;
import com.codingchili.core.Routing.Model.ListenerSettings;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 *         root game server, deploys realmName servers.
 */
public class Server extends ClusterVerticle {
    private RoutingSettings settings;

    public Server() {
        this.settings = FileConfiguration.instance().getRoutingSettings();
    }

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        this.logger = new DefaultLogger(vertx, settings.getLogserver());
    }

    @Override
    public void start(Future<Void> start) {
        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {

            for (ListenerSettings listener : settings.getTransport()) {
                RouteProvider provider = new RouteProvider(vertx);

                switch (listener.getType()) {
                    case UDP:
                        vertx.deployVerticle(new UdpListener(new RouteHandler(provider), listener));
                        break;
                    case TCP:
                        vertx.deployVerticle(new TcpListener(new RouteHandler(provider), listener));
                        break;
                    case WEBSOCKET:
                        vertx.deployVerticle(new WebsocketListener(new RouteHandler(provider), listener));
                        break;
                    case REST:
                        vertx.deployVerticle(new RestListener(new RouteHandler(provider), settings));
                        break;
                }
            }
        }

        logger.onServerStarted(start);
    }
}
