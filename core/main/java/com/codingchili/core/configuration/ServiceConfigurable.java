package com.codingchili.core.configuration;


import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Robin Duda
 * <p>
 * Service configurables are loaded by service contexts for use in services.
 */
public class ServiceConfigurable extends BaseConfigurable {
    private String node = "undefined";

    public ServiceConfigurable() {
    }

    public ServiceConfigurable(String path) {
        super(path);
    }

    @JsonIgnore
    public String host() {
        return Environment.hostname().orElse("undefined");
    }

    @JsonIgnore
    public String node() {
        return node;
    }

    public String getNode() {
        return node();
    }

    public ServiceConfigurable setNode(String node) {
        this.node = node;
        return this;
    }
}
