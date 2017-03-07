package com.codingchili.router.model;

/**
 * @author Robin Duda
 *         <p>
 *         Used to map API routes to another target/route
 */
public class Endpoint {
    private String target;
    private String route;

    public Endpoint() {
    }

    /**
     * @param target endpoint target node
     */
    public Endpoint(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }

    public Endpoint setTarget(String target) {
        this.target = target;
        return this;
    }

    public String getRoute() {
        return route;
    }

    public Endpoint setRoute(String route) {
        this.route = route;
        return this;
    }
}
