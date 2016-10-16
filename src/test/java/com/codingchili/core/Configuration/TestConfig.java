package com.codingchili.core.Configuration;

/**
 * @author Robin Duda
 */
class TestConfig implements LoadableConfigurable {
    private String data;

    @Override
    public String getPath() {
        return "test.json";
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
