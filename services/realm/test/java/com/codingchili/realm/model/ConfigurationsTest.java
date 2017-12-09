package com.codingchili.realm.model;

import com.codingchili.core.files.ConfigurationFactory;
import com.codingchili.core.files.exception.NoSuchResourceException;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.realm.instance.model.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static com.codingchili.core.configuration.CoreStrings.testDirectory;
import static com.codingchili.core.configuration.CoreStrings.testFile;

/**
 * @author Robin Duda
 * tests the loading of json files used for configuration storage.
 */
@RunWith(VertxUnitRunner.class)
public class ConfigurationsTest {

    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);

    @Test
    public void readMissingFile(TestContext test) {
        try {
            ConfigurationFactory.readObject("missing/file.json");
            test.fail("No exception on missing file.");
        } catch (NoSuchResourceException ignored) {
        }
    }

    @Test
    public void readDirectoryObjects() throws IOException {
        ConfigurationFactory.readDirectoryObjects(testDirectory("class"));
    }

    @Test
    public void testReadAfflictions(TestContext test) throws IOException {
        AfflictionList afflictions = Serializer.unpack(ConfigurationFactory.readObject(testFile("affliction.json")), AfflictionList.class);
        test.assertNotNull(afflictions);
        test.assertNotEquals(0, afflictions.getAfflictions().size());
    }

    @Test
    public void testReadPlayerClasses() throws IOException {
        Collection<JsonObject> classes = ConfigurationFactory.readDirectoryObjects(testDirectory("class"));

        for (JsonObject player : classes) {
            Serializer.unpack(player, PlayerClass.class);
        }

        Assert.assertFalse(classes.isEmpty());
    }

    @Test
    public void testReadPlayerTemplate() throws IOException {
        JsonObject template = ConfigurationFactory.readObject(testFile("character.json"));
        PlayerCharacter player = Serializer.unpack(template, PlayerCharacter.class);

        Assert.assertNotNull(player);
        Assert.assertFalse(player.getAttributes().isEmpty());

        Inventory inventory = player.getInventory();
        Assert.assertFalse(inventory.getEquipped().isEmpty());
        Assert.assertFalse(inventory.getItems().isEmpty());
    }
}
