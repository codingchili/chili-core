package com.codingchili.core.configuration;

/**
 * @author Robin Duda
 *
 * Represents a basic configurable that is saveable.
 */
public class BaseConfigurable implements Configurable {
    protected String path;

    public BaseConfigurable() {}

    public BaseConfigurable(String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public BaseConfigurable setPath(String path) {
        this.path = path;
        return this;
    }
}
