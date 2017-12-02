package com.codingchili.core.protocol;

import java.util.*;

import com.codingchili.core.protocol.exception.AuthorizationRequiredException;
import com.codingchili.core.protocol.exception.HandlerMissingException;

/**
 * @author Robin Duda
 * <p>
 * Maps roles to routes.
 * <p>
 * A route with an access level of 25 is not accessible by
 * other roles with the same access level. To share a single
 * route with multiple roles with the same access level
 * assign multiple roles to a route.
 * <p>
 * A role with a higher access level may execute any route
 * protected by a role with a weaker access level.
 */
public class SimpleAuthorizationHandler<T> implements AuthorizationHandler<T> {
    // holds all handlers grouped on roles => routes => route::handler.
    private final Map<RoleType, HashMap<String, Route<T>>> handlers = new HashMap<>();
    // holds all existing routes, to allow checking for missing routes vs unauthorized.
    private final Set<String> routes = new HashSet<>();

    @Override
    public void use(Route<T> route) {
        for (RoleType role : route.getRole()) {
            if (!handlers.containsKey(role)) {
                handlers.put(role, new HashMap<>());
            }
            handlers.get(role).put(route.getName(), route);
        }
        routes.add(route.getName());
    }

    @Override
    public RequestHandler<T> get(String route, RoleType role) throws AuthorizationRequiredException, HandlerMissingException {
        if (contains(route)) {
            if (handlers.containsKey(role)) {
                Route<T> api = handlers.get(role).get(route);
                if (api != null) {
                    return api.getHandler();
                }
            }
            // no exact role match on ID, do a full 2nd level scan to check access level.
            for (RoleType required : handlers.keySet()) {
                if (required.getLevel() < role.getLevel()) {
                    Route<T> api = handlers.get(required).get(route);
                    if (api != null) {
                        return api.getHandler();
                    }
                }
            }
        } else {
            throw new HandlerMissingException(route);
        }
        throw new AuthorizationRequiredException();
    }

    @Override
    public boolean contains(String route) {
        return routes.contains(route);
    }

    @Override
    public List<Route<T>> list() {
        Set<Route<T>> routes = new HashSet<>();
        handlers.values().forEach(role -> routes.addAll(role.values()));
        return new ArrayList<>(routes);
    }
}
