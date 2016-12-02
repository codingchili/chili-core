package com.codingchili.core.configuration;

/**
 * @author Robin Duda
 *
 * A configurable implementation for use in tests.
 */
public class ConfigurableTest extends BaseConfigurable {
    private String data = "default-data";

    public ConfigurableTest() {
        this.path = CoreStrings.testFile("Configurations", "testfile.json");
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
