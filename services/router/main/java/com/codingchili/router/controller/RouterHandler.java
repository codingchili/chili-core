package com.codingchili.router.controller;

import com.codingchili.core.listener.BusRouter;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.Protocol;
import com.codingchili.core.protocol.Role;
import com.codingchili.core.protocol.exception.AuthorizationRequiredException;
import com.codingchili.router.configuration.RouterContext;

import static com.codingchili.common.Strings.ANY;
import static com.codingchili.common.Strings.NODE_ROUTER;

/**
 * @author Robin Duda
 * <p>
 * Forwards messages to other nodes from an input transport.
 * Adds target filtering to the BusRouter.
 */
public class RouterHandler extends BusRouter {
    private final Protocol<Request> protocol = new Protocol<>();
    private RouterContext context;

    public RouterHandler(RouterContext context) {
        this.context = context;
        protocol.use(ANY, super::handle)
                .use(NODE_ROUTER, Request::accept);
    }

    @Override
    public void handle(Request request) {
        if (context.isRouteExternal(request.target(), request.route())) {
            protocol.get(request.target(), Role.USER).submit(request);
        } else {
            request.error(new AuthorizationRequiredException(
                    String.format("Requested target '%s' and route '%s' is not allowed.",
                            request.target(), request.route())));
        }
    }

    @Override
    public String address() {
        return NODE_ROUTER;
    }
}
