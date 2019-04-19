package com.codingchili.core.protocol;

import com.fasterxml.jackson.annotation.*;

import java.util.*;

/**
 * Contains protocol mappings and descriptions.
 */
public class ProtocolDescription<T> {
    private String description;
    private String target;
    private Map<String, String> model;
    private Map<String, Route<T>> routes = new HashMap<>();

    public ProtocolDescription() {
    }

    /**
     * @param template the class template to use as a base for all requests.
     * @return fluent
     */
    @JsonIgnore
    public ProtocolDescription<T> setTemplate(Class<?> template) {
        if (template != null) {
            model = Serializer.describe(template);
        }
        return this;
    }

    /**
     * @return the base template to be used by all requests to this api, authentication etc.
     */
    public Map<String, String> getModel() {
        return model;
    }

    /**
     * @param model the template described as a serialized model with {@link Serializer#describe(Class)}
     * @return fluent
     */
    public ProtocolDescription setModel(Map<String, String> model) {
        this.model = model;
        return this;
    }

    /**
     * @return a description of the api.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description of the api.
     * @return fluent
     */
    public ProtocolDescription<T> setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * @return a map of routes that are available on the endpoint.
     */
    public Map<String, Route<T>> getRoutes() {
        return routes;
    }

    /**
     * @param routes a list of routes to be included in the generated api documentation.
     * @return fluent
     */
    @JsonIgnore
    public ProtocolDescription<T> setRoutes(List<Route<T>> routes) {
        routes.forEach(route -> this.routes.put(route.getRoute(), route));
        return this;
    }

    /**
     * @param routes a map of routes to be included in the generated api documentation.
     * @return fluent
     */
    @JsonProperty("routes")
    public ProtocolDescription<T> setRoutes(Map<String, Route<T>> routes) {
        this.routes = routes;
        return this;
    }

    /**
     * @param route a route to be included in the protocol description.
     * @return fluent
     */
    public ProtocolDescription<T> addRoute(Route<T> route) {
        this.routes.put(route.getRoute(), route);
        return this;
    }

    /**
     * @return the endpoint (target) at which this api is mounted.
     */
    public String getTarget() {
        return target;
    }

    /**
     * @param target the target node at which this api is available.
     * @return fluent
     */
    public ProtocolDescription<T> setTarget(String target) {
        this.target = target;
        return this;
    }
}
