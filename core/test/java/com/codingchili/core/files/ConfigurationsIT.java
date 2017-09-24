package com.codingchili.core.files;

import com.codingchili.core.configuration.Configurable;
import com.codingchili.core.configuration.ConfigurableTest;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.codingchili.core.configuration.CoreStrings.*;


/**
 * @author Robin Duda
 * <p>
 * Most configuration files are mocked during test, this suite
 * verifies that the configuration files exists and are loaded correctly.
 */
@RunWith(VertxUnitRunner.class)
public class ConfigurationsIT {
    private static final String TEST_PATH = testFile("Configurations", "testfile.json");
    private static final String NEW_DATA = "new-data";
    private static final String TEST_DATA = "test-data";
    private static final String CONFIGURATIONS = "Configurations";

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
        String directory = testDirectory(CONFIGURATIONS);

        Configurations.available(directory)
                .forEach(path -> Assert.assertTrue(path.startsWith(directory)));
    }

    @Test
    public void clearLoadedFiles() {
        // Assert some files are loaded..
        Assert.assertTrue(Configurations.loaded().stream()
                .map(Configurable::getPath)
                .anyMatch(config -> true));

        Configurations.unload();

        Assert.assertFalse(Configurations.loaded().stream()
                .filter(configurable -> !configurable.getPath().startsWith(DIR_SYSTEM))
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
}
