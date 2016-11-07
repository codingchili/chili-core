package com.codingchili.services.Router.Model;

/**
 * @author Robin Duda
 */
public class Endpoint {
    private String target;
    private String action;

    public String getTarget() {
        return target;
    }

    public Endpoint setTarget(String target) {
        this.target = target;
        return this;
    }

    public String getAction() {
        return action;
    }

    public Endpoint setAction(String action) {
        this.action = action;
        return this;
    }
}
