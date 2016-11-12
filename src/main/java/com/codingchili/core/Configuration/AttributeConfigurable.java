package com.codingchili.core.Configuration;

/**
 * @author Robin Duda
 *
 * Simple configurable that maps to a json object.
 */
public class AttributeConfigurable extends Attributes implements Configurable {
    private String path;

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }
}
