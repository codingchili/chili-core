package Routing.Controller;

import Protocols.*;
import Routing.Configuration.RouteProvider;
import Routing.Model.ClusterRequest;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;

import static Configuration.Strings.*;

/**
 * @author Robin Duda
 */
public class RoutingHandler extends HandlerProvider {
    private Vertx vertx;

    public RoutingHandler(RouteProvider provider) {
        super(RoutingHandler.class, provider.getLogger(), ANY);
        this.vertx = provider.getVertx();
    }

    @Authenticator
    public Access authorize(Request request) {
        return Access.AUTHORIZED;
    }

    @Handles(ANY)
    public void webserver(Request request) {
        sendCluster(NODE_WEBSERVER, request);
    }

    @Handles(NODE_REALM)
    public void realm(Request request) {
        try {
            sendCluster(getRealm(request), request);
        } catch (TargetNodeUnspecifiedException e) {
            request.error();
        }
    }

    private String getRealm(Request request) throws TargetNodeUnspecifiedException {
        JsonObject data = request.data();

        if (data.containsKey(ID_TARGET)) {
            return data.getString(ID_TARGET);
        } else {
            throw new TargetNodeUnspecifiedException();
        }
    }

    @Handles(NODE_LOGGING)
    public void logging(ClusterRequest request) {
        sendCluster(NODE_LOGGING, request);
    }

    @Handles(NODE_PATCHING)
    public void patching(ClusterRequest request) {
        sendCluster(NODE_PATCHING, request);
    }

    @Handles(NODE_AUTHHENTICATION_CLIENTS)
    public void clientAuthentication(ClusterRequest request) {
        sendCluster(NODE_AUTHHENTICATION_CLIENTS, request);
    }

    @Handles(NODE_AUTHENTICATION_REALMS)
    public void realmAuthentication(ClusterRequest request) {
        sendCluster(NODE_AUTHENTICATION_REALMS, request);
    }

    private void sendCluster(String address, Request request) {
        DeliveryOptions options = new DeliveryOptions().setSendTimeout(request.timeout());

        vertx.eventBus().send(address, request.data(), options, result -> {
            if (result.succeeded()) {
                request.write(result.result().body());
            } else {
                request.missing();
            }
        });
    }
}
