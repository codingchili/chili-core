package com.codingchili.core.Configuration;

/**
 * @author Robin Duda
 *
 * A configurable implementation for use in tests.
 */
public class ConfigurableTest extends BaseConfigurable {
    private String data = "default-data";

    public ConfigurableTest() {
        this.path = Strings.testFile("Configurations", "testfile.json");
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