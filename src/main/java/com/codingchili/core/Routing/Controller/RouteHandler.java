package com.codingchili.core.Routing.Controller;

import com.codingchili.core.Protocols.AbstractHandler;
import com.codingchili.core.Protocols.Exception.ProtocolException;
import com.codingchili.core.Protocols.Request;
import com.codingchili.core.Protocols.RequestHandler;
import com.codingchili.core.Protocols.Util.Protocol;
import com.codingchili.core.Protocols.Util.Validator;
import com.codingchili.core.Routing.Configuration.RouteProvider;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;

import static com.codingchili.core.Configuration.Strings.*;
import static com.codingchili.core.Protocols.Access.AUTHORIZED;

/**
 * @author Robin Duda
 */
public class RouteHandler extends AbstractHandler {
    private Validator validator = new Validator();
    private final Protocol<RequestHandler<Request>> protocol = new Protocol<>();
    private final Vertx vertx;

    public RouteHandler(RouteProvider provider) {
        super(NODE_ROUTING);
        this.logger = provider.getLogger();
        this.vertx = provider.getVertx();

        protocol.use(ANY, this::dynamic)
                .use(NODE_WEBSERVER, this::webserver)
                .use(NODE_REALM, this::realm)
                .use(NODE_LOGGING, this::logging)
                .use(NODE_PATCHING, this::patching)
                .use(NODE_AUTHENTICATION_REALMS, this::realmAuthentication)
                .use(NODE_AUTHENTICATION_CLIENTS, this::clientAuthentication)
                .use(ID_PING, request -> request.write(null));
    }

    @Override
    public void handle(Request request) throws ProtocolException {
        try {
            validator.validate(request.data());
            protocol.get(AUTHORIZED, request.target()).handle(request);
        } catch (Validator.RequestValidationException e) {
            request.bad();
        }
    }

    /**
     * Handler for dynamic routes and requests missing target.
     *
     * @param request the request to be routed.
     */
    private void dynamic(Request request) {
        if (request.target().endsWith(NODE_REALM)) {
            realm(request);
        } else if (request.target().startsWith("/")) {
            webserver(request);
        } else {
            request.missing();
        }
    }

    private void webserver(Request request) {
        sendCluster(NODE_WEBSERVER, request);
    }

    private void logging(Request request) {
        sendCluster(NODE_LOGGING, request);
    }

    private void patching(Request request) {
        sendCluster(NODE_PATCHING, request);
    }

    private void clientAuthentication(Request request) {
        sendCluster(NODE_AUTHENTICATION_CLIENTS, request);
    }

    private void realmAuthentication(Request request) {
        sendCluster(NODE_AUTHENTICATION_REALMS, request);
    }

    private void realm(Request request) {
        sendCluster(request.target(), request);
    }

    private void sendCluster(String address, Request request) {
        DeliveryOptions options = new DeliveryOptions().setSendTimeout(request.timeout());

        vertx.eventBus().send(address, request.data(), options, send -> {
            if (send.succeeded()) {
                request.write(send.result().body());
            } else {
                request.missing();
            }
        });
    }
}
