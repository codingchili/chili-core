package com.codingchili.core.Configuration;

/**
 * @author Robin Duda
 */
public class TestConfig implements Configurable {
    private String path = "test.json";
    private String data;

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
