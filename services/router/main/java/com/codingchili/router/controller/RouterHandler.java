package com.codingchili.router.controller;

import com.codingchili.router.configuration.RouterContext;
import io.vertx.core.eventbus.*;

import com.codingchili.core.context.CoreException;
import com.codingchili.core.protocol.*;
import com.codingchili.core.protocol.exception.AuthorizationRequiredException;
import com.codingchili.core.protocol.exception.RequestValidationException;
import com.codingchili.core.security.Validator;

import static com.codingchili.common.Strings.*;
import static com.codingchili.core.protocol.Access.AUTHORIZED;

/**
 * @author Robin Duda
 *
 * Forwards messages to other nodes from an input transport.
 */
public class RouterHandler<T extends RouterContext> extends AbstractHandler<T> {
    private final Protocol<RequestHandler<Request>> protocol = new Protocol<>();
    private final Validator validator = new Validator();

    public RouterHandler(T context) {
        super(context, NODE_ROUTER);

        protocol.use(ANY, this::sendCluster)
                .use(NODE_ROUTER, Request::accept);
    }

    @Override
    public void handle(Request request) throws CoreException {
        try {
            if (context.isRouteHidden(request.target())) {
                request.error(new AuthorizationRequiredException());
            } else {
                validator.validate(request.data());
                protocol.get(AUTHORIZED, request.target()).handle(request);
            }
        } catch (RequestValidationException e) {
            request.error(e);
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
}
