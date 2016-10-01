package Routing.Controller;

import Logging.Model.Logger;
import Protocols.*;
import Protocols.Exception.AuthorizationRequiredException;
import Protocols.Exception.HandlerMissingException;
import Protocols.Util.Protocol;
import Routing.Configuration.RouteProvider;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;

import static Configuration.Strings.*;
import static Protocols.Access.AUTHORIZED;

/**
 * @author Robin Duda
 */
public class RoutingHandler extends AbstractHandler {
    private Protocol<RequestHandler<Request>> protocol = new Protocol<>();
    private Logger logger;
    private Vertx vertx;

    public RoutingHandler(RouteProvider provider) {
        super(ANY);
        this.logger = provider.getLogger();
        this.vertx = provider.getVertx();

        protocol.use(ANY, this::webserver)
                .use(NODE_REALM, this::realm)
                .use(NODE_LOGGING, this::logging)
                .use(NODE_PATCHING, this::patching)
                .use(NODE_AUTHENTICATION_REALMS, this::realmAuthentication)
                .use(NODE_AUTHHENTICATION_CLIENTS, this::clientAuthentication);
    }

    @Override
    public void handle(Request request) {
        try {
            protocol.get(AUTHORIZED, request.target()).handle(request);
        } catch (AuthorizationRequiredException e) {
            request.unauthorized();
        } catch (HandlerMissingException e) {
            request.missing();
            logger.onHandlerMissing(request.target());
        }
    }

    private void webserver(Request request) {
        sendCluster(NODE_WEBSERVER, request);
    }

    private void realm(Request request) {
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

    private void logging(Request request) {
        sendCluster(NODE_LOGGING, request);
    }

    private void patching(Request request) {
        sendCluster(NODE_PATCHING, request);
    }

    private void clientAuthentication(Request request) {
        sendCluster(NODE_AUTHHENTICATION_CLIENTS, request);
    }

    private void realmAuthentication(Request request) {
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
