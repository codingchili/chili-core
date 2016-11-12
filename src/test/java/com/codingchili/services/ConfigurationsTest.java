package com.codingchili.services;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.Timeout;
import org.junit.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.codingchili.core.Configuration.Configurable;
import com.codingchili.core.Files.Configurations;
import com.codingchili.core.Files.JsonFileStore;
import com.codingchili.core.Protocol.Serializer;

import com.codingchili.services.Authentication.Configuration.AuthServerSettings;
import com.codingchili.services.Logging.Configuration.LogServerSettings;
import com.codingchili.services.Patching.Configuration.PatchServerSettings;
import com.codingchili.services.Realm.Configuration.RealmServerSettings;
import com.codingchili.services.Realm.Instance.Model.*;
import com.codingchili.services.Router.Configuration.RouterSettings;
import com.codingchili.services.Social.Configuration.SocialSettings;
import com.codingchili.services.Website.Configuration.WebserverSettings;

import static com.codingchili.services.Authentication.Configuration.AuthServerSettings.PATH_AUTHSERVER;
import static com.codingchili.services.Logging.Configuration.LogServerSettings.PATH_LOGSERVER;
import static com.codingchili.services.Patching.Configuration.PatchServerSettings.PATH_PATCHSERVER;
import static com.codingchili.services.Realm.Configuration.RealmServerSettings.PATH_REALMSERVER;
import static com.codingchili.services.Router.Configuration.RouterSettings.PATH_ROUTING;
import static com.codingchili.services.Shared.Strings.*;
import static com.codingchili.services.Social.Configuration.SocialSettings.PATH_SOCIAL;
import static com.codingchili.services.Website.Configuration.WebserverSettings.PATH_WEBSERVER;

/**
 * @author Robin Duda
 *         tests the loading of json files used for configuration storage.
 */

public class ConfigurationsTest {

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
    public void testReadPlayerTemplate() throws IOException {
        JsonObject template = JsonFileStore.readObject(PATH_GAME_PLAYERTEMPLATE);
        PlayerCharacter player = Serializer.unpack(template, PlayerCharacter.class);

        Assert.assertNotNull(player);
        Assert.assertFalse(player.getAttributes().isEmpty());

        Inventory inventory = player.getInventory();
        Assert.assertFalse(inventory.getEquipped().isEmpty());
        Assert.assertFalse(inventory.getItems().isEmpty());
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
        Assert.assertNotNull(load(PATH_ROUTING, RouterSettings.class));
    }

    @Test
    public void loadSocialConfiguration() {
        Assert.assertNotNull(load(PATH_SOCIAL, SocialSettings.class));
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

    private Configurable load(String path, Class clazz) {
        return Configurations.get(path, clazz);
    }
}
