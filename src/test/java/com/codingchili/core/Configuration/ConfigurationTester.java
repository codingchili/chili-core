package com.codingchili.core.Configuration;

import com.codingchili.core.Authentication.Configuration.AuthServerSettings;
import com.codingchili.core.Realm.Configuration.RealmServerSettings;
import com.codingchili.core.Realm.Configuration.RealmSettings;
import com.codingchili.core.Realm.Instance.Model.Affliction;
import com.codingchili.core.Realm.Instance.Model.Inventory;
import com.codingchili.core.Realm.Instance.Model.PlayerCharacter;
import com.codingchili.core.Realm.Instance.Model.PlayerClass;
import com.codingchili.core.Protocols.Util.Serializer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.Timeout;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.codingchili.core.Configuration.Strings.PATH_GAME_AFFLICTIONS;
import static com.codingchili.core.Configuration.Strings.PATH_GAME_CLASSES;
import static com.codingchili.core.Configuration.Strings.PATH_GAME_PLAYERTEMPLATE;

/**
 * @author Robin Duda
 *         tests the loading of json files used for configuration storage.
 */

public class ConfigurationTester {

    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);

    @Test
    public void testReadList() throws IOException {
        JsonFileStore.readList("conf/game/player/affliction.json");
    }

    @Test
    public void readMissingFile() {
        try {
            JsonFileStore.readObject("missing/file.json");
            throw new RuntimeException("No exception on missing file.");
        } catch (IOException ignored) {
        }
    }

    @Test
    public void readDirectoryObjects() throws IOException {
        JsonFileStore.readDirectoryObjects("conf/game/class/");
    }

    @Test
    public void testReadAfflictions() throws IOException {
        JsonArray afflictions = JsonFileStore.readList(PATH_GAME_AFFLICTIONS);

        for (int i = 0; i < afflictions.size(); i++) {
            Serializer.unpack(afflictions.getJsonObject(i), Affliction.class);
        }

        Assert.assertFalse(afflictions.isEmpty());
    }

    @Test
    public void testReadPlayerClasses() throws IOException {
        ArrayList<JsonObject> classes = JsonFileStore.readDirectoryObjects(PATH_GAME_CLASSES);

        for (JsonObject player : classes) {
            Serializer.unpack(player, PlayerClass.class);
        }

        Assert.assertFalse(classes.isEmpty());
    }

    @Test
    public void testTrustedRealm() {
        RealmServerSettings server = FileConfiguration.instance().getRealmServerSettings();
        AuthServerSettings auth = FileConfiguration.instance().getAuthSettings();

        for (RealmSettings realm : server.getRealms()) {
            Assert.assertTrue(auth.isTrustedRealm(realm.getName()));
        }
    }

    @Test
    public void testNotTrustedRealm() {
        Assert.assertFalse(new AuthServerSettings().isTrustedRealm("not trusted"));
    }

    @Test
    public void testReadPlayerTemplate() throws IOException {
        JsonObject template = JsonFileStore.readObject(PATH_GAME_PLAYERTEMPLATE);
        PlayerCharacter player = Serializer.unpack(template, PlayerCharacter.class);

        Assert.assertNotNull(player);
        Assert.assertFalse(player.getAttributes().isEmpty());

        Inventory inventory = player.getInventory();
        Assert.assertFalse(inventory.getEquipped().isEmpty());
        Assert.assertFalse(inventory.getItems().isEmpty());
    }
}
