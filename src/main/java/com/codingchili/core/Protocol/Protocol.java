package com.codingchili.core.Protocol;

import com.codingchili.core.Exception.AuthorizationRequiredException;
import com.codingchili.core.Exception.HandlerMissingException;

import static com.codingchili.core.Configuration.Strings.ANY;

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

    public Protocol<Handler> use(String action, Handler handler) {
        use(action, handler, Access.AUTHORIZED);
        return this;
    }

    public Protocol<Handler> use(String action, Handler handler, Access access) {
        handlers.use(action, handler, access);
        return this;
    }

    public ProtocolMapping list() {
        return handlers.list();
    }
}

