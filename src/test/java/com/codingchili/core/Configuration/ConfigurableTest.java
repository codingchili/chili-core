package com.codingchili.core.Configuration;

/**
 * @author Robin Duda
 */
public class ConfigurableTest extends BaseConfigurable {
    private String data;

    public ConfigurableTest() {
        this.path = "src/test/resources/Configurations/testconfig.json";
    }

    public ConfigurableTest(String path) {
        this.path = path;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
