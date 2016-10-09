package com.codingchili.core.Configuration;

import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Robin Duda
 *         <p>
 *         Most configuration files are mocked during test, this suite
 *         verifies that the configuration files exists and are loaded correctly.
 */
@RunWith(VertxUnitRunner.class)
public class FileConfigurationTest {
    private static ConfigurationLoader config;

    @BeforeClass
    public static void setUp() {
        config = FileConfiguration.instance();
    }

    @Test
    public void loadVertxConfiguration() {
        Assert.assertNotNull(config.getVertxSettings());
    }

    @Test
    public void loadAuthenticationConfiguration() {
        Assert.assertNotNull(config.getAuthSettings());
    }

    @Test
    public void loadRealmServerConfiguration() {
        Assert.assertNotNull(config.getRealmServerSettings());
    }

    @Test
    public void loadRoutingConfiguration() {
        Assert.assertNotNull(config.getRoutingSettings());
    }

    @Test
    public void loadLoggingConfiguration() {
        Assert.assertNotNull(config.getLogSettings());
    }

    @Test
    public void loadWebsiteConfiguration() {
        Assert.assertNotNull(config.getWebsiteSettings());
    }

    @Test
    public void loadPatchingConfiguration() {
        Assert.assertNotNull(config.getPatchServerSettings());
    }

    @Test
    public void loadDeployConfiguration() {
        Assert.assertNotNull(config.getDeploySettings());

    }

    @Test
    public void loadLauncherConfiguration() {
        Assert.assertNotNull(config.getLauncherSettings());

    }
}
