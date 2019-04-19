package com.codingchili.core.protocol;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Map;

import com.codingchili.core.configuration.CoreStrings;

import static com.codingchili.core.protocol.RoleMap.USER;

/**
 * Models a route in the Protocol.
 *
 * @param <T> type of the handler to handle the route.
 */
public class Route<T> {
    @JsonIgnore
    private RequestHandler<T> handler;
    private String description = CoreStrings.getDescriptionMissing();
    private Map<String, String> model;
    private RoleType[] roles;
    private String route;

    /**
     * Empty constructor for serialization support, the handler is not serialized.
     */
    public Route() {
    }

    /**
     * @param route {@link #Route(String, RequestHandler, RoleType...)}
     */
    public Route(String route) {
        this.route = route;
    }

    /**
     * @param route   {@link #Route(String, RequestHandler, RoleType...)}
     * @param handler {@link #Route(String, RequestHandler, RoleType...)}
     */
    public Route(String route, RequestHandler<T> handler) {
        this(route, handler, RoleMap.get(USER));
    }

    /**
     * @param route   a text representation of where this route is mounted at, relative to the target handler.
     * @param handler the handler to be invoked when the route matches an incoming request.
     * @param role    a set of roles that have access to this route.
     */
    public Route(String route, RequestHandler<T> handler, RoleType... role) {
        this.handler = handler;
        this.route = route;
        this.roles = role;
    }

    @JsonIgnore
    public String getRoute() {
        return route;
    }

    /**
     * @param route {@link #Route(String, RequestHandler, RoleType...)}
     * @return fluent
     */
    public Route<T> setRoute(String route) {
        this.route = route;
        return this;
    }

    /**
     * @return a description of this route.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description a text description of this route.
     * @return fluent
     */
    public Route<T> setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * @return a list of roles that are authorized to invoke the endpoint.
     */
    public RoleType[] getRoles() {
        return roles;
    }

    /**
     * @param roles a list of roles that are allowed access to this api.
     * @return fluent
     */
    public Route<T> setRoles(Role... roles) {
        this.roles = roles;
        return this;
    }

    /**
     * @return the handler that is invoked by the request processor when the request
     * route matches this route in the protocol.
     */
    public RequestHandler<T> getHandler() {
        return handler;
    }

    /**
     * @param handler see {@link #getHandler()}
     * @return fluent.
     */
    public Route<T> setHandler(RequestHandler<T> handler) {
        this.handler = handler;
        return this;
    }

    @Override
    public int hashCode() {
        return route.hashCode();
    }

    /**
     * @param template the class template to generate a text description of.
     * @return fluent
     */
    public Route<T> setTemplate(Class<?> template) {
        this.model = Serializer.describe(template);
        return this;
    }

    /**
     * @param model a map of key value pairs as attribute name and type, describes
     *              the input/output of the api method.
     * @return fluent
     */
    public Route<T> setModel(Map<String, String> model) {
        this.model = model;
        return this;
    }

    /**
     * @return a text representation of the object graph that the route expect as input.
     */
    public Map<String, String> getModel() {
        return model;
    }
}
