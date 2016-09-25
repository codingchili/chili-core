package Routing;

import Configuration.FileConfiguration;
import Protocols.ClusterVerticle;
import Routing.Configuration.RouteProvider;
import Routing.Configuration.RoutingSettings;
import Routing.Controller.Transport.RestListener;
import Routing.Controller.Transport.TcpListener;
import Routing.Controller.Transport.UdpListener;
import Routing.Controller.Transport.WebsocketListener;
import Routing.Model.ListenerSettings;
import io.vertx.core.Future;

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
    public void start(Future<Void> start) {
        RouteProvider provider = new RouteProvider(vertx, settings);

        this.logger = provider.getLogger();

        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {

            for (ListenerSettings listener : settings.getTransport()) {
                switch (listener.getType()) {
                    case UDP:
                        vertx.deployVerticle(new UdpListener(provider, listener));
                        break;
                    case TCP:
                        vertx.deployVerticle(new TcpListener(provider, listener));
                        break;
                    case WEBSOCKET:
                        vertx.deployVerticle(new WebsocketListener(provider, listener));
                        break;
                    case REST:
                        vertx.deployVerticle(new RestListener(provider, listener));
                        break;
                }
            }
        }

        logger.onServerStarted(start);
    }

    @Override
    public void stop(Future<Void> stop) throws Exception {
        logger.onServerStopped(stop);
    }
}
