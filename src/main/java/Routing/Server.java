package Routing;

import Configuration.FileConfiguration;
import Logging.Model.DefaultLogger;
import Protocols.ClusterVerticle;
import Routing.Configuration.RouteProvider;
import Routing.Configuration.RoutingSettings;
import Routing.Controller.RoutingHandler;
import Routing.Controller.Transport.RestListener;
import Routing.Controller.Transport.TcpListener;
import Routing.Controller.Transport.UdpListener;
import Routing.Controller.Transport.WebsocketListener;
import Routing.Model.ListenerSettings;
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
                RouteProvider provider = new RouteProvider(vertx, settings);

                switch (listener.getType()) {
                    case UDP:
                        vertx.deployVerticle(new UdpListener(new RoutingHandler(provider), listener));
                        break;
                    case TCP:
                        vertx.deployVerticle(new TcpListener(new RoutingHandler(provider), listener));
                        break;
                    case WEBSOCKET:
                        vertx.deployVerticle(new WebsocketListener(new RoutingHandler(provider), listener));
                        break;
                    case REST:
                        vertx.deployVerticle(new RestListener(new RoutingHandler(provider), settings));
                        break;
                }
            }
        }

        logger.onServerStarted(start);
    }
}
