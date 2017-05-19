package com.codingchili.router.controller;

import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.Protocol;
import com.codingchili.core.protocol.RequestHandler;
import com.codingchili.core.protocol.exception.AuthorizationRequiredException;
import com.codingchili.router.configuration.RouterContext;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;

import static com.codingchili.common.Strings.*;
import static com.codingchili.core.protocol.Access.AUTHORIZED;

/**
 * @author Robin Duda
 *
 * Forwards messages to other nodes from an input transport.
 */
public class RouterHandler implements CoreHandler {
    private final Protocol<RequestHandler<Request>> protocol = new Protocol<>();
    private RouterContext context;

    public RouterHandler(RouterContext context) {
        this.context = context;
        protocol.use(ANY, this::sendCluster)
                .use(NODE_ROUTER, Request::accept);
    }

    @Override
    public void handle(Request request) {
        if (context.isRouteHidden(request.target())) {
            request.error(new AuthorizationRequiredException());
        } else {
            protocol.get(AUTHORIZED, request.target()).handle(request);
        }
    }

    private void sendCluster(Request request) {
        DeliveryOptions options = new DeliveryOptions().setSendTimeout(request.timeout());

        context.bus().send(request.target(), request.data(), options, send -> {
            if (send.succeeded()) {
                request.write(send.result().body());
            } else {
                Throwable exception = send.cause();

                if (exception instanceof ReplyException) {
                    ReplyFailure status = ((ReplyException) exception).failureType();

                    switch (status) {
                        case TIMEOUT:
                            context.onNodeTimeout(request.target(), request.route(), request.timeout());
                            request.error(new RequestTimedOutException(request));
                            break;
                        case NO_HANDLERS:
                            context.onNodeNotReachable(request.target());
                            request.error(new NodeNotReachableException(request));
                            break;
                        case RECIPIENT_FAILURE:
                            context.onRecipientFailure(request.target(), request.route());
                            request.error(new NodeFailedToAcknowledge(request));
                            break;
                    }
                } else {
                    request.error(send.cause());
                }
            }
        });
    }

    @Override
    public String address() {
        return NODE_ROUTER;
    }
}
