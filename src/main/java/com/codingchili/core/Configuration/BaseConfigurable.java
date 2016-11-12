package com.codingchili.core.Configuration;

/**
 * @author Robin Duda
 *
 * Simplest form of configurable, only contains the path from where it
 * was loaded.
 */
public class BaseConfigurable implements Configurable {
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
