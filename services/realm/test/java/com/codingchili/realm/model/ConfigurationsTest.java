package com.codingchili.realm.model;

import com.codingchili.core.files.ConfigurationFactory;
import com.codingchili.core.files.exception.NoSuchResourceException;
import com.codingchili.core.protocol.Serializer;

import com.codingchili.realm.instance.model.entity.PlayerCreature;
import com.codingchili.realm.instance.model.entity.PlayableClass;
import com.codingchili.realm.instance.model.items.Inventory;
import com.codingchili.realm.instance.model.afflictions.AfflictionList;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
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
        ConfigurationFactory.readDirectory(testDirectory("class"));
    }

    @Test
    @Ignore("affliction format is being reworked.")
    public void testReadAfflictions(TestContext test) throws IOException {
        AfflictionList afflictions = Serializer.unpack(ConfigurationFactory.readObject(testFile("affliction.json")), AfflictionList.class);
        test.assertNotNull(afflictions);
        test.assertNotEquals(0, afflictions.getAfflictions().size());
    }

    @Test
    public void testReadPlayerClasses() throws IOException {
        Collection<JsonObject> classes = ConfigurationFactory.readDirectory(testDirectory("class"));

        for (JsonObject player : classes) {
            Serializer.unpack(player, PlayableClass.class);
        }

        Assert.assertFalse(classes.isEmpty());
    }

    @Ignore("Templates in progress.")
    public void testReadPlayerTemplate() throws IOException {
        JsonObject template = ConfigurationFactory.readObject(testFile("character.json"));
        PlayerCreature player = Serializer.unpack(template, PlayerCreature.class);

        Assert.assertNotNull(player);
        Assert.assertFalse(player.getBaseStats().isEmpty());
        Inventory inventory = player.getInventory();
        Assert.assertFalse(inventory.getEquipped().isEmpty());
        Assert.assertFalse(inventory.getItems().isEmpty());
    }
}
