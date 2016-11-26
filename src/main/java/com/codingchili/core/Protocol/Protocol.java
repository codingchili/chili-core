package com.codingchili.core.protocol;

import com.codingchili.core.protocol.exception.AuthorizationRequiredException;
import com.codingchili.core.protocol.exception.HandlerMissingException;

import static com.codingchili.core.configuration.Strings.ANY;

/**
 * @author Robin Duda
 */
public class Protocol<Handler extends RequestHandler> {
    private final AuthorizationHandler<Handler> handlers = new AuthorizationHandler<>();

    public Handler get(Access access, String target) throws AuthorizationRequiredException, HandlerMissingException {
        if (handlers.contains(target)) {
            return handlers.get(target, access);
        } else {
            return handlers.get(ANY, access);
        }
    }

    public Handler get(String target) throws AuthorizationRequiredException, HandlerMissingException {
        return get(Access.AUTHORIZED, target);
    }

    public Protocol<Handler> use(String route, Handler handler) {
        use(route, handler, Access.AUTHORIZED);
        return this;
    }

    public Protocol<Handler> use(String route, Handler handler, Access access) {
        handlers.use(route, handler, access);
        return this;
    }

    public ProtocolMapping list() {
        return handlers.list();
    }
}

