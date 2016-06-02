package Protocols;

import Protocols.AuthorizationHandler.Access;
import Protocols.Exception.AuthorizationRequiredException;
import Protocols.Exception.HandlerMissingException;

/**
 * @author Robin Duda
 */
public class Protocol<Handler extends PacketHandler> {
    private AuthorizationHandler<Handler> handlers;

    public Protocol() {
        this.handlers = new AuthorizationHandler<>();
    }

    public Protocol(Access access) {
        this.handlers = new AuthorizationHandler<>(access);
    }

    public Protocol<Handler> use(String action, Handler handler) {
        handlers.use(action, handler);
        return this;
    }

    public Protocol<Handler> use(String action, Handler handler, Access access) {
        handlers.use(action, handler, access);
        return this;
    }

    public Handler get(String action, Access access) throws AuthorizationRequiredException, HandlerMissingException {
        return handlers.get(action, access);
    }
}

