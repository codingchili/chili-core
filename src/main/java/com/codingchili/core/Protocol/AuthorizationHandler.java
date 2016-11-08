package com.codingchili.core.Protocol;

import java.util.HashMap;

import com.codingchili.core.Exception.AuthorizationRequiredException;
import com.codingchili.core.Exception.HandlerMissingException;

/**
 * @author Robin Duda
 */
class AuthorizationHandler<T> {
    private final HashMap<String, T> authorized = new HashMap<>();
    private final HashMap<String, T> unauthorized = new HashMap<>();

    public void use(String action, T handler, Access access) {
        switch (access) {
            case PUBLIC:
                unauthorized.put(action, handler);
                break;
            case AUTHORIZED:
                authorized.put(action, handler);
                break;
        }
    }

    public T get(String action, Access access) throws AuthorizationRequiredException, HandlerMissingException {
        switch (access) {
            case PUBLIC:
                return unauthorized(action);
            case AUTHORIZED:
                return any(action);
            default:
                throw new AuthorizationRequiredException();
        }
    }

    public boolean contains(String action) {
        return (authorized.containsKey(action) || unauthorized.containsKey(action));
    }

    private T unauthorized(String action) throws AuthorizationRequiredException, HandlerMissingException {
        if (unauthorized.containsKey(action)) {
            return unauthorized.get(action);
        } else if (authorized.containsKey(action)) {
            throw new AuthorizationRequiredException();
        } else {
            throw new HandlerMissingException();
        }
    }

    private T any(String action) throws HandlerMissingException {
        if (authorized.containsKey(action)) {
            return authorized.get(action);
        } else if (unauthorized.containsKey(action)) {
            return unauthorized.get(action);
        } else {
            throw new HandlerMissingException();
        }
    }

    public HashMap<String, T> list() {
        HashMap<String, T> list = new HashMap<>();
        list.putAll(authorized);
        list.putAll(unauthorized);
        return list;
    }
}
