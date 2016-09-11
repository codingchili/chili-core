package Routing.Controller;

import Configuration.Strings;
import Routing.Configuration.RouteProvider;
import Routing.Model.ClusterRequest;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;

/**
 * @author Robin Duda
 */
public class RoutingHandler {
    private Vertx vertx;

    public RoutingHandler(RouteProvider provider) {
        this.vertx = provider.getVertx();

        provider.getProtocol()
                .use(Strings.ADDRESS_AUTHENTICATION, this::authentication)
                .use(Strings.ADDRESS_WEBSERVER, this::webserver)
                .use(Strings.ADDRESS_PATCHING, this::patching)
                .use(Strings.ADDRESS_LOGGING, this::logging)
                .use(Strings.ADDRESS_REALM, this::realm);
    }

    private void realm(ClusterRequest request) {
        sendCluster(realmAddress(request), request);
    }

    private String realmAddress(ClusterRequest request) {
        return Strings.ADDRESS_REALM + "." + request.realm() + "." + request.instance();
    }

    private void logging(ClusterRequest request) {
        sendCluster(Strings.ADDRESS_LOGGING, request);
    }

    private void patching(ClusterRequest request) {
        sendCluster(Strings.ADDRESS_PATCHING, request);
    }

    private void webserver(ClusterRequest request) {
        sendCluster(Strings.ADDRESS_WEBSERVER, request);
    }

    private void authentication(ClusterRequest request) {
        sendCluster(Strings.ADDRESS_AUTHENTICATION, request);
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
