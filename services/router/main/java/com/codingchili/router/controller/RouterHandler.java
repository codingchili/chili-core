package com.codingchili.router.controller;

import com.codingchili.core.listener.BusRouter;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.Protocol;
import com.codingchili.core.protocol.RequestHandler;
import com.codingchili.core.protocol.exception.AuthorizationRequiredException;
import com.codingchili.router.configuration.RouterContext;

import static com.codingchili.common.Strings.ANY;
import static com.codingchili.common.Strings.NODE_ROUTER;
import static com.codingchili.core.protocol.Access.AUTHORIZED;

/**
 * @author Robin Duda
 *
 * Forwards messages to other nodes from an input transport.
 * Adds target filtering to the BusRouter.
 */
public class RouterHandler extends BusRouter {
    private final Protocol<RequestHandler<Request>> protocol = new Protocol<>();
    private RouterContext context;

    public RouterHandler(RouterContext context) {
        this.context = context;
        protocol.use(ANY, super::handle)
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

    @Override
    public String address() {
        return NODE_ROUTER;
    }
}
