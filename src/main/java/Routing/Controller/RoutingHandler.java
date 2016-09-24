package Routing.Controller;

import Configuration.Strings;
import Protocols.Handles;
import Routing.Configuration.RouteProvider;
import Routing.Model.ClusterRequest;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;

import static Configuration.Strings.*;

/**
 * @author Robin Duda
 */
public class RoutingHandler {
    private Vertx vertx;

    public RoutingHandler(RouteProvider provider) {
        this.vertx = provider.getVertx();
    }

    @Handles(ADDRESS_REALM)
    private void realm(ClusterRequest request) {
        sendCluster(realmAddress(request), request);
    }

    private String realmAddress(ClusterRequest request) {
        return Strings.ADDRESS_REALM + "." + request.realm() + "." + request.instance();
    }

    @Handles(ADDRESS_LOGGING)
    private void logging(ClusterRequest request) {
        sendCluster(Strings.ADDRESS_LOGGING, request);
    }

    @Handles(ADDRESS_PATCHING)
    private void patching(ClusterRequest request) {
        sendCluster(Strings.ADDRESS_PATCHING, request);
    }

    @Handles(ADDRESS_WEBSERVER)
    private void webserver(ClusterRequest request) {
        sendCluster(Strings.ADDRESS_WEBSERVER, request);
    }

    @Handles(ADDRESS_AUTHENTICATION_CLIENTS)
    private void clientAuthentication(ClusterRequest request) {
        sendCluster(ADDRESS_AUTHENTICATION_CLIENTS, request);
    }

    @Handles(ADDRESS_AUTHENTICATION_REALMS)
    private void realmAuthentication(ClusterRequest request) {
        sendCluster(ADDRESS_AUTHENTICATION_REALMS, request);
    }

    private void sendCluster(String address, ClusterRequest request) {
        DeliveryOptions options = new DeliveryOptions().setSendTimeout(request.getTimeout());

        vertx.eventBus().send(address, request.getMessage(), options, result -> {
            if (result.succeeded()) {
                request.write(result.result().body());
            } else {
                request.missing();
            }
        });
    }
}
