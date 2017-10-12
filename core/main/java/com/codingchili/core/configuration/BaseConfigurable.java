package com.codingchili.core.configuration;

/**
 * @author Robin Duda
 * <p>
 * Represents a basic configurable that is saveable.
 */
public class BaseConfigurable implements Configurable {
    protected String path;

    public BaseConfigurable() {
    }

    public BaseConfigurable(String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }
}
