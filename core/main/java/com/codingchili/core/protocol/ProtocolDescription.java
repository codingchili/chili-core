package com.codingchili.core.protocol;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains protocol mappings and descriptions.
 */
public class ProtocolDescription<T> {
    private Map<String, String> model = new HashMap<>();
    private String text;
    private List<Route<T>> routes;

    public ProtocolDescription() {
    }

    public ProtocolDescription(Class<?> template, List<Route<T>> routes, String text) {
        if (template != null) {
            model = Serializer.describe(template);
        }
        this.routes = routes;
        this.text = text;
    }

    public Map<String, String> getModel() {
        return model;
    }

    public void setModel(Map<String, String> model) {
        this.model = model;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Route<T>> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route<T>> routes) {
        this.routes = routes;
    }
}
