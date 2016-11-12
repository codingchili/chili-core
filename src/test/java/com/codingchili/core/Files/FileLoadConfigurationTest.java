package com.codingchili.core.Files;

import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import com.codingchili.core.Configuration.*;
import com.codingchili.core.Configuration.System.SystemSettings;
import com.codingchili.core.Exception.FileReadException;

import static com.codingchili.core.Configuration.Strings.DIR_SERVICES;
import static com.codingchili.core.Configuration.Strings.PATH_VERTX;


/**
 * @author Robin Duda
 *
 * Most configuration files are mocked during test, this suite
 * verifies that the configuration files exists and are loaded correctly.
 */
@RunWith(VertxUnitRunner.class)
public class FileLoadConfigurationTest {
    private static final String NEW_DATA = "new-data";
    private static final String TEST_DATA = "test-data";

    @AfterClass
    public static void tearDown() {
        JsonFileStore.deleteObject(new TestConfig().getPath());
    }

    @Test
    public void loadVertxConfiguration() {
        Assert.assertNotNull(load(PATH_VERTX, SystemSettings.class));
    }

    @Test
    public void loadMissingFile() {
        try {
            load("x", TestConfig.class);
            throw new RuntimeException("Should throw FileReadEx on missing file.");
        } catch (FileReadException ignored) {
        }
    }

    @Test
    public void reloadSingleFile() {
        TestConfig config = new TestConfig();

        Configurations.save(config);
        config = Configurations.get(config.getPath(), TestConfig.class);

        config.setData(NEW_DATA);
        Configurations.save(config);
        config.setData(TEST_DATA);    // restore memory copy

        Configurations.reload(config.getPath());

        config = Configurations.get(config.getPath(), TestConfig.class);

        Assert.assertTrue(config.getData().equals(NEW_DATA));
    }

    @Test
    public void fileCachedAndNotAlwaysReloaded() {
        TestConfig config = new TestConfig();

        Configurations.save(config);
        config = Configurations.get(config.getPath(), TestConfig.class);

        config.setData(NEW_DATA);
        Configurations.save(config);
        config.setData(TEST_DATA);    // restore memory copy
        config = Configurations.get(config.getPath(), TestConfig.class);

        Assert.assertFalse(config.getData().equals(NEW_DATA));
    }

    @Test
    public void saveAConfigurableToFile() {
        TestConfig config = new TestConfig();

        Configurations.save(config);
        config = Configurations.get(config.getPath(), TestConfig.class);

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

    private Configurable load(String path, Class clazz) {
        return Configurations.get(path, clazz);
    }
}
