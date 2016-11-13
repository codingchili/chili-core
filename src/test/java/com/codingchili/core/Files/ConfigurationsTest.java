package com.codingchili.core.Files;

import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import com.codingchili.core.Configuration.Configurable;
import com.codingchili.core.Configuration.ConfigurableTest;
import com.codingchili.core.Exception.FileReadException;

/**
 * @author Robin Duda
 */
@RunWith(VertxUnitRunner.class)
public class ConfigurationsTest {

    @Test
    public void loadLauncherConfiguration() throws IOException {
        Assert.assertNotNull(Configurations.launcher());
    }

    @Test
    public void loadValidatorConfiguration() {
        Assert.assertNotNull(Configurations.validator());
    }

    @Test
    public void loadSystemConfiguration() {
        Assert.assertNotNull(Configurations.system());
    }

    @Test
    public void loadSecurityConfiguration() {
        Assert.assertNotNull(Configurations.security());
    }

    @Test
    public void loadMissingFile() {
        try {
            load("x", ConfigurableTest.class);
            throw new RuntimeException("Should throw FileReadEx on missing file.");
        } catch (FileReadException ignored) {
        }
    }

    private Configurable load(String path, Class clazz) {
        return Configurations.get(path, clazz);
    }
}
