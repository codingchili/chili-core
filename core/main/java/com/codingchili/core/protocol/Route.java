package com.codingchili.core.protocol;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
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
    private Map<String, String> model = new HashMap<>();
    private String description = CoreStrings.getDescriptionMissing();
    private RoleType[] role;
    private String route;

    public Route(String route, RequestHandler<T> handler) {
        this(route, handler, RoleMap.get(USER));
    }

    public Route(String route, RequestHandler<T> handler, RoleType... role) {
        this.handler = handler;
        this.route = route;
        this.role = role;
    }

    public String getDescription() {
        return description;
    }

    public Route<T> setDescription(String description) {
        this.description = description;
        return this;
    }

    public RoleType[] getRole() {
        return role;
    }

    public Route<T> setRole(Role[] role) {
        this.role = role;
        return this;
    }

    public String getName() {
        return route;
    }

    public Route<T> setRoute(String route) {
        this.route = route;
        return this;
    }

    public RequestHandler<T> getHandler() {
        return handler;
    }

    public Route<T> setHandler(RequestHandler<T> handler) {
        this.handler = handler;
        return this;
    }

    @Override
    public int hashCode() {
        return route.hashCode();
    }

    public void setModel(Class<?> model) {
        this.model = Serializer.describe(model);
    }

    public void setModel(Map<String, String> model) {
        this.model = model;
    }

    public Map<String, String> getModel() {
        return model;
    }
}
