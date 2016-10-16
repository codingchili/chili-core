package com.codingchili.core.Configuration;

import com.codingchili.core.Authentication.Configuration.AuthServerSettings;
import com.codingchili.core.Configuration.System.DeploySettings;
import com.codingchili.core.Configuration.System.LauncherSettings;
import com.codingchili.core.Configuration.System.VertxSettings;
import com.codingchili.core.Logging.Configuration.LogServerSettings;
import com.codingchili.core.Patching.Configuration.PatchServerSettings;
import com.codingchili.core.Realm.Configuration.RealmServerSettings;
import com.codingchili.core.Routing.Configuration.RoutingSettings;
import com.codingchili.core.Website.Configuration.WebserverSettings;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.codingchili.core.Configuration.Strings.*;

/**
 * @author Robin Duda
 *         <p>
 *         Most configuration files are mocked during test, this suite
 *         verifies that the configuration files exists and are loaded correctly.
 */
@RunWith(VertxUnitRunner.class)
public class FileConfigurationTest {

    @Before
    public void setUp() {

    }

    @AfterClass
    public static void tearDown() {
        JsonFileStore.deleteObject(new TestConfig().getPath());
    }

    @Test
    public void loadVertxConfiguration() {
        Assert.assertNotNull(load(PATH_VERTX, VertxSettings.class));
    }

    @Test
    public void loadAuthenticationConfiguration() {
        Assert.assertNotNull(load(PATH_AUTHSERVER, AuthServerSettings.class));
    }

    @Test
    public void loadRealmServerConfiguration() {
        Assert.assertNotNull(load(PATH_REALMSERVER, RealmServerSettings.class));
    }

    @Test
    public void loadRoutingConfiguration() {
        Assert.assertNotNull(load(PATH_ROUTING, RoutingSettings.class));
    }

    @Test
    public void loadLoggingConfiguration() {
        Assert.assertNotNull(load(PATH_LOGSERVER, LogServerSettings.class));
    }

    @Test
    public void loadWebsiteConfiguration() {
        Assert.assertNotNull(load(PATH_WEBSERVER, WebserverSettings.class));
    }

    @Test
    public void loadPatchingConfiguration() {
        Assert.assertNotNull(load(PATH_PATCHSERVER, PatchServerSettings.class));
    }

    @Test
    public void loadDeployConfiguration() {
        Assert.assertNotNull(load(PATH_DEPLOY, DeploySettings.class));
    }

    @Test
    public void loadLauncherConfiguration() {
        Assert.assertNotNull(load(PATH_LAUNCHER, LauncherSettings.class));
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

        FileConfiguration.save(config);
        config = FileConfiguration.get(config.getPath(), TestConfig.class);

        config.setData("new-data");
        FileConfiguration.save(config);
        config.setData("test-data");    // restore memory copy

        FileConfiguration.reload(config.getPath());
        config = FileConfiguration.get(config.getPath(), TestConfig.class);

        Assert.assertTrue(config.getData().equals("new-data"));
    }

    @Test
    public void fileCachedAndNotAlwaysReloaded() {
        TestConfig config = new TestConfig();

        FileConfiguration.save(config);
        config = FileConfiguration.get(config.getPath(), TestConfig.class);

        config.setData("new-data");
        FileConfiguration.save(config);
        config.setData("test-data");    // restore memory copy
        config = FileConfiguration.get(config.getPath(), TestConfig.class);

        Assert.assertFalse(config.getData().equals("new-data"));
    }

    @Test
    public void saveAConfigurableToFile() {
        TestConfig config = new TestConfig();

        FileConfiguration.save(config);
        config = FileConfiguration.get(config.getPath(), TestConfig.class);

        Assert.assertTrue(config.getData().equals("test-data"));
    }

    @Test
    public void listAllLoaded() {
        Assert.assertTrue(FileConfiguration.loaded().stream()
                .map(LoadableConfigurable::getPath)
                .anyMatch(config -> true));
    }

    @Test
    public void listAllAvailable() {
        Assert.assertTrue(FileConfiguration.available().stream()
                .anyMatch(path -> true));
    }

    @Test
    public void listAllAvailableOnSubpath() {
        FileConfiguration.available(PATH_REALM).stream()
                .forEach(path -> Assert.assertTrue(path.startsWith(PATH_REALM)));
    }

    @Test
    public void clearLoadedFiles() {
        // Assert some files are loaded..
        Assert.assertTrue(FileConfiguration.loaded().stream()
                .map(LoadableConfigurable::getPath)
                .anyMatch(config -> true));

        FileConfiguration.unload();

        Assert.assertFalse(FileConfiguration.loaded().stream()
                .map(LoadableConfigurable::getPath)
                .anyMatch(config -> true));
    }

    @Test
    public void getOverriddenFilePath() {
        Assert.assertEquals(
                PATH_GAME_PLAYERTEMPLATE.replace(PATH_GAME, PATH_GAME_OVERRIDE + "sample"),
                FileConfiguration.override(PATH_GAME_PLAYERTEMPLATE, "sample")
        );
    }

    @Test
    public void getNonOverridenFilePath() {
        Assert.assertNotEquals(
                PATH_GAME_PLAYERTEMPLATE.replace(PATH_GAME, PATH_GAME_OVERRIDE + "sample-nx"),
                FileConfiguration.override(PATH_GAME_PLAYERTEMPLATE, "sample-nx")
        );
    }

    private LoadableConfigurable load(String path, Class clazz) {
        return FileConfiguration.get(path, clazz);
    }
}
