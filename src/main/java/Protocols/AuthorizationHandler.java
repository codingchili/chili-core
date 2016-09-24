package Protocols;

import Protocols.Exception.AuthorizationRequiredException;
import Protocols.Exception.HandlerMissingException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Robin Duda
 */
public class AuthorizationHandler {
    private HashMap<String, Method> authorized = new HashMap<>();
    private HashMap<String, Method> unauthorized = new HashMap<>();

    public void use(String action, Method handler, Access access) {
        switch (access) {
            case PUBLIC:
                unauthorized.put(action, handler);
                break;
            case AUTHORIZE:
                authorized.put(action, handler);
                break;
        }
    }

    public Method get(String action, Access access) throws AuthorizationRequiredException, HandlerMissingException {
        switch (access) {
            case PUBLIC:
                return unauthorized(action);
            case AUTHORIZE:
                return any(action);
            default:
                throw new AuthorizationRequiredException();
        }
    }

    private Method unauthorized(String action) throws AuthorizationRequiredException, HandlerMissingException {
        if (unauthorized.containsKey(action)) {
            return unauthorized.get(action);
        } else if (authorized.containsKey(action)) {
            throw new AuthorizationRequiredException();
        } else {
            throw new HandlerMissingException();
        }
    }

    private Method any(String action) throws HandlerMissingException {
        if (authorized.containsKey(action)) {
            return authorized.get(action);
        } else if (unauthorized.containsKey(action)) {
            return unauthorized.get(action);
        } else {
            throw new HandlerMissingException();
        }
    }

    public HashMap<String, Method> list() {
        HashMap<String, Method> list = new HashMap<>();
        list.putAll(authorized);
        list.putAll(unauthorized);
        return list;
    }
}
