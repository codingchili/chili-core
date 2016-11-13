package com.codingchili.core.Files;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import com.codingchili.core.Configuration.Configurable;
import com.codingchili.core.Configuration.ConfigurableTest;
import com.codingchili.core.Exception.FileReadException;

import static com.codingchili.core.Configuration.Strings.DIR_SERVICES;


/**
 * @author Robin Duda
 *         <p>
 *         Most configuration files are mocked during test, this suite
 *         verifies that the configuration files exists and are loaded correctly.
 */
@RunWith(VertxUnitRunner.class)
public class ConfigurationsIT {
    private static final String TEST_PATH = "src/main/test/resources/Configurations/testfile.json";
    private static final String NEW_DATA = "new-data";
    private static final String TEST_DATA = "test-data";

    @Test
    public void reloadSingleFile() {
        ConfigurableTest config = new ConfigurableTest();

        Configurations.save(config);
        config = Configurations.get(config.getPath(), ConfigurableTest.class);

        config.setData(NEW_DATA);
        Configurations.save(config);
        config.setData(TEST_DATA);    // restore memory copy

        Configurations.reload(config.getPath());

        config = Configurations.get(config.getPath(), ConfigurableTest.class);

        Assert.assertTrue(config.getData().equals(NEW_DATA));
    }

    @Test
    public void fileCachedAndNotAlwaysReloaded() {
        ConfigurableTest config = new ConfigurableTest();

        Configurations.save(config);
        config = Configurations.get(config.getPath(), ConfigurableTest.class);

        config.setData(NEW_DATA);
        Configurations.save(config);
        config.setData(TEST_DATA);    // restore memory copy
        config = Configurations.get(config.getPath(), ConfigurableTest.class);

        Assert.assertFalse(config.getData().equals(NEW_DATA));
    }

    @Test
    public void saveAConfigurableToFile() {
        ConfigurableTest config = new ConfigurableTest();

        Configurations.save(config);
        config = Configurations.get(config.getPath(), ConfigurableTest.class);

        Assert.assertTrue(config.getData().equals(TEST_DATA));
    }

    @Test
    public void listAllLoaded() {
        Assert.assertTrue(Configurations.loaded().stream()
                .map(Configurable::getPath)
                .anyMatch(config -> true));
    }

    @Test
    public void listAllAvailable() {
        Assert.assertTrue(Configurations.available().stream()
                .anyMatch(path -> true));
    }

    @Test
    public void listAllAvailableOnSubpath() {
        Configurations.available(DIR_SERVICES).stream()
                .forEach(path -> Assert.assertTrue(path.startsWith(DIR_SERVICES)));
    }

    @Test
    public void clearLoadedFiles() {
        // Assert some files are loaded..
        Assert.assertTrue(Configurations.loaded().stream()
                .map(Configurable::getPath)
                .anyMatch(config -> true));

        Configurations.unload();

        Assert.assertFalse(Configurations.loaded().stream()
                .map(Configurable::getPath)
                .anyMatch(config -> true));
    }

    @Test
    public void testPutConfiguration(TestContext test) {
        ConfigurableTest configurable = new ConfigurableTest(TEST_PATH);
        configurable.setData(NEW_DATA);

        Configurations.put(configurable);

        ConfigurableTest loaded = Configurations.get(TEST_PATH, ConfigurableTest.class);
        test.assertEquals(NEW_DATA, loaded.getData());
    }

    private Configurable load(String path, Class clazz) {
        return Configurations.get(path, clazz);
    }
}
