package com.codingchili.core.protocol;

import java.util.HashMap;

import com.codingchili.core.protocol.exception.AuthorizationRequiredException;
import com.codingchili.core.protocol.exception.HandlerMissingException;

/**
 * @author Robin Duda
 *
 * Handles authorization for a protoocol.
 */
class AuthorizationHandler<T> {
    private final HashMap<String, T> authorized = new HashMap<>();
    private final HashMap<String, T> unauthorized = new HashMap<>();

    public void use(String route, T handler, Access access) {
        switch (access) {
            case PUBLIC:
                unauthorized.put(route, handler);
                break;
            case AUTHORIZED:
                authorized.put(route, handler);
                break;
        }
    }

    T get(String route, Access access) throws AuthorizationRequiredException, HandlerMissingException {
        switch (access) {
            case PUBLIC:
                return unauthorized(route);
            case AUTHORIZED:
                return any(route);
            default:
                throw new AuthorizationRequiredException();
        }
    }

    boolean contains(String route) {
        return (authorized.containsKey(route) || unauthorized.containsKey(route));
    }

    private T unauthorized(String route) throws AuthorizationRequiredException, HandlerMissingException {
        if (unauthorized.containsKey(route)) {
            return unauthorized.get(route);
        } else if (authorized.containsKey(route)) {
            throw new AuthorizationRequiredException();
        } else {
            throw new HandlerMissingException(route);
        }
    }

    private T any(String route) throws HandlerMissingException {
        if (authorized.containsKey(route)) {
            return authorized.get(route);
        } else if (unauthorized.containsKey(route)) {
            return unauthorized.get(route);
        } else {
            throw new HandlerMissingException(route);
        }
    }

    ProtocolMapping list() {
        ProtocolMapping list = new ProtocolMapping();

        authorized.entrySet().forEach(entry -> list.add(entry.getKey(), Access.AUTHORIZED));
        unauthorized.entrySet().forEach(entry -> list.add(entry.getKey(), Access.PUBLIC));
        return list;
    }
}
