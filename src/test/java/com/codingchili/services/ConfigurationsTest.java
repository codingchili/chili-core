package com.codingchili.services;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.Timeout;
import org.junit.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.codingchili.core.configuration.Configurable;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.files.JsonFileStore;
import com.codingchili.core.protocol.Serializer;

import com.codingchili.services.authentication.configuration.AuthServerSettings;
import com.codingchili.services.logging.configuration.LogServerSettings;
import com.codingchili.services.patching.configuration.PatchServerSettings;
import com.codingchili.services.realm.configuration.RealmServerSettings;
import com.codingchili.services.realm.instance.model.*;
import com.codingchili.services.router.configuration.RouterSettings;
import com.codingchili.services.Social.configuration.SocialSettings;
import com.codingchili.services.website.configuration.WebserverSettings;

import static com.codingchili.services.Shared.Strings.*;

/**
 * @author Robin Duda
 *         tests the loading of json files used for configuration storage.
 */

public class ConfigurationsTest {

    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);

    @Test
    public void testReadList() throws IOException {
        JsonFileStore.readList(testFile("Game", "affliction.json"));
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
        JsonFileStore.readDirectoryObjects(testDirectory("Game/class"));
    }

    @Test
    public void testTrustedRealm() {
        AuthServerSettings auth = new AuthServerSettings();

        auth.getRealms().add("realmName");

        Assert.assertTrue(auth.isTrustedRealm("realmName"));
    }

    @Test
    public void testNotTrustedRealm() {
        Assert.assertFalse(new AuthServerSettings().isTrustedRealm("not trusted"));
    }

    @Test
    public void testReadAfflictions() throws IOException {
        JsonArray afflictions = JsonFileStore.readList(testFile("Game/afflictions", "affliction.json"));

        for (int i = 0; i < afflictions.size(); i++) {
            Serializer.unpack(afflictions.getJsonObject(i), Affliction.class);
        }

        Assert.assertFalse(afflictions.isEmpty());
    }

    @Test
    public void testReadPlayerClasses() throws IOException {
        ArrayList<JsonObject> classes = JsonFileStore.readDirectoryObjects(testDirectory("Game/class"));

        for (JsonObject player : classes) {
            Serializer.unpack(player, PlayerClass.class);
        }

        Assert.assertFalse(classes.isEmpty());
    }

    @Test
    public void testReadPlayerTemplate() throws IOException {
        JsonObject template = JsonFileStore.readObject(testFile("Game", "character.json"));
        PlayerCharacter player = Serializer.unpack(template, PlayerCharacter.class);

        Assert.assertNotNull(player);
        Assert.assertFalse(player.getAttributes().isEmpty());

        Inventory inventory = player.getInventory();
        Assert.assertFalse(inventory.getEquipped().isEmpty());
        Assert.assertFalse(inventory.getItems().isEmpty());
    }

    @Test
    public void loadAuthenticationConfiguration() {
        Assert.assertNotNull(load(EMPTY, AuthServerSettings.class));
    }

    @Test
    public void loadRealmServerConfiguration() {
        Assert.assertNotNull(load(EMPTY, RealmServerSettings.class));
    }

    @Test
    public void loadRoutingConfiguration() {
        Assert.assertNotNull(load(EMPTY, RouterSettings.class));
    }

    @Test
    public void loadSocialConfiguration() {
        Assert.assertNotNull(load(EMPTY, SocialSettings.class));
    }

    @Test
    public void loadLoggingConfiguration() {
        Assert.assertNotNull(load(EMPTY, LogServerSettings.class));
    }

    @Test
    public void loadWebsiteConfiguration() {
        Assert.assertNotNull(load(EMPTY, WebserverSettings.class));
    }

    @Test
    public void loadPatchingConfiguration() {
        Assert.assertNotNull(load(EMPTY, PatchServerSettings.class));
    }

    private Configurable load(String path, Class clazz) {
        return Configurations.get(path, clazz);
    }
}
