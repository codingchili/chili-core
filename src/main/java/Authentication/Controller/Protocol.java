package Authentication.Controller;

import Authentication.Model.AuthorizationHandler;
import Authentication.Model.AuthorizationHandler.Access;
import Authentication.Model.AuthorizationRequiredException;
import Authentication.Model.HandlerMissingException;

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


    public static final String AUTHENTICATEl = "realmName.register";
    public static final String CLOSE = "connection.close";
    public static final String CHARACTERLIST = "character-list";
    public static final String CHARACTERCREATE = "character-create";
    public static final String CHARACTERREMOVE = "character-remove";
    public static final String AUTHENTICATE = "authenticate";
    public static final String REGISTER = "register";
    public static final String REALMTOKEN = "realmtoken";
    public static final String REALMLIST = "realmlist";
}

