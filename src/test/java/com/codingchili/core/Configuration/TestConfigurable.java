package com.codingchili.core.Configuration;

/**
 * @author Robin Duda
 */
public class TestConfigurable extends BaseConfigurable {
    private String data;

    public TestConfigurable() {
        this.path = "src/test/resources/Configurations/testconfig.json";
    }

    public TestConfigurable(String path) {
        this.path = path;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
