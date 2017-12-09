package com.codingchili.core.protocol;

import com.codingchili.core.protocol.exception.AuthorizationRequiredException;
import com.codingchili.core.protocol.exception.HandlerMissingException;

import java.util.List;

/**
 * Handles the mapping of routes to roles.
 *
 * @param <T> type of requests to check authorization for
 */
public interface AuthorizationHandler<T> {

    /**
     * Adds a new route to the handler.
     *
     * @param route the route to add
     */
    void use(Route<T> route);

    /**
     * Retrieves the RequestHandler of a route matching the
     * route id and that any of the given roles have permission to.
     *
     * @param route the id of the route to retrieve
     * @param role the role used in the authorization check
     * @return a request handler used for the route.
     * @throws AuthorizationRequiredException when a route exists but none
     *                                        of the given roles has permission to execute it.
     * @throws HandlerMissingException        when the requested route is missing.
     */
    RequestHandler<T> get(String route, RoleType role) throws AuthorizationRequiredException, HandlerMissingException;

    /**
     * @param route id of the route to check if it is contained in the handler.
     * @return true if the route is registered.
     */
    boolean contains(String route);

    /**
     * @return all routes that has been added to the handler.
     */
    List<Route<T>> list();
}
