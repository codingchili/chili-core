package com.codingchili.core.configuration;

import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.codingchili.core.files.Configurations;

import static com.codingchili.services.Shared.Strings.*;

/**
 * @author Robin Duda
 */
@RunWith(VertxUnitRunner.class)
public class SettingsOverrideTest {
    private static final String FILENAME = "testconfig.json";
    private static final String CONFIG_DIR = "Configurations";
    private static final String OVERRIDE_DIR = "Configurations/override";
    private static final String MISSING_OVERRIDE_DIR = "configuration/override-nx";

    @Test
    public void getOverriddenFilePath() {
        Assert.assertEquals(
                testFile(OVERRIDE_DIR, FILENAME),
                Configurations.override(
                        testDirectory(CONFIG_DIR),
                        testDirectory(OVERRIDE_DIR),
                        testFile(CONFIG_DIR, FILENAME)));
    }

    @Test
    public void getNonOverridenFilePath() {
        Assert.assertEquals(
                testFile(CONFIG_DIR, FILENAME),
                Configurations.override(
                        testDirectory(CONFIG_DIR),
                        testDirectory(MISSING_OVERRIDE_DIR),
                        testFile(CONFIG_DIR, FILENAME)));
    }
}
