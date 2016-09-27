package Protocols;

import Protocols.Exception.AuthorizationRequiredException;
import Protocols.Exception.HandlerMissingException;

import static Configuration.Strings.ANY;

/**
 * @author Robin Duda
 */
public class Protocol<Handler extends PacketHandler> {
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

