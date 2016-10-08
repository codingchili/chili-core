package com.codingchili.core.Protocols.Util;

import com.codingchili.core.Protocols.Access;
import com.codingchili.core.Protocols.Exception.AuthorizationRequiredException;
import com.codingchili.core.Protocols.Exception.HandlerMissingException;
import com.codingchili.core.Protocols.RequestHandler;

import static com.codingchili.core.Configuration.Strings.ANY;

/**
 * @author Robin Duda
 */
public class Protocol<Handler extends RequestHandler> {
    private AuthorizationHandler<Handler> handlers = new AuthorizationHandler<>();

    public Handler get(Access access, String target) throws AuthorizationRequiredException, HandlerMissingException {
        if (handlers.contains(target)) {
            return handlers.get(target, access);
        } else {
            return handlers.get(ANY, access);
        }
    }

    public Protocol<Handler> use(String action, Handler handler) {
        use(action, handler, Access.AUTHORIZED);
        return this;
    }

    public Protocol<Handler> use(String action, Handler handler, Access access) {
        handlers.use(action, handler, access);
        return this;
    }
}

