package com.codingchili.services.Router.Controller;

import io.vertx.core.eventbus.DeliveryOptions;

import com.codingchili.core.Exception.*;
import com.codingchili.core.Protocol.*;
import com.codingchili.core.Security.Validator;

import com.codingchili.services.Router.Configuration.RouterContext;

import static com.codingchili.services.Shared.Strings.*;
import static com.codingchili.core.Protocol.Access.AUTHORIZED;

/**
 * @author Robin Duda
 */
public class RouterHandler<T extends RouterContext> extends AbstractHandler<T> {
    private final Protocol<RequestHandler<Request>> protocol = new Protocol<>();
    private final Validator validator = new Validator();

    public RouterHandler(T context) {
        super(context, NODE_ROUTING);

        protocol.use(ANY, this::sendCluster)
                .use(NODE_ROUTING, Request::accept);
    }

    @Override
    public void handle(Request request) throws CoreException {
        try {
            if (context.isRouteHidden(request.target())) {
                request.unauthorized(new AuthorizationRequiredException());
            } else {
                validator.validate(request.data());
                protocol.get(AUTHORIZED, request.target()).handle(request);
            }
        } catch (RequestValidationException e) {
            request.bad(e);
        }
    }

    private void sendCluster(Request request) {
        DeliveryOptions options = new DeliveryOptions().setSendTimeout(request.timeout());

        context.bus().send(request.target(), request.data(), options, send -> {
            if (send.succeeded()) {
                request.write(send.result().body());
            } else {
                request.error(new RequestTimedOutException(request.target(), request.timeout()));
            }
        });
    }
}
