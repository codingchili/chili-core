package com.codingchili.core.Configuration;

import com.codingchili.core.Realm.Configuration.RealmSettings;
import com.codingchili.core.Realm.Instance.Configuration.InstanceSettings;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static com.codingchili.core.Configuration.Strings.*;

/**
 * @author Robin Duda
 */

@RunWith(VertxUnitRunner.class)
public class RealmSettingsTest {
    private static String realmName = "testRealm";
    private static String realmFile = "conf/realm/testRealm.json";
    private static String instanceName = "test-instance";
    private static String instanceDir = "conf/realm/override/testRealm/instances";
    private static String instanceOverrideFile = "conf/realm/override/testRealm/instances/" + instanceName + ".json";
    private static String instanceFile = "conf/game/instances/" + instanceName + ".json";
    private static String realmDir = "conf/realm/override/testRealm";
    private static JsonObject backup;

    @Before
    public void setUp() {
        FileConfiguration.unload();
    }

    @AfterClass
    public static void tearDown() {
        restoreMockConfiguration();
    }

    @Test
    public void testConfigurationOverride(TestContext context) throws IOException {
        createMockConfiguration(true);

        RealmSettings realm = getRealm();
        context.assertEquals(1, realm.getInstance().size());
        context.assertEquals(realmName, realm.getName());
        InstanceSettings instance = realm.getInstance().get(0);
        context.assertEquals(instance.getName(), instanceName);

        restoreMockConfiguration();
    }

    @Test
    public void testConfigurationNotOverridden(TestContext context) throws IOException {
        createMockConfiguration(false);

        RealmSettings realm = getRealm();

        // Assert test instance not in configured instances.
        realm.getInstance().stream()
                .map(InstanceSettings::getName)
                .forEach(name -> context.assertFalse(name.equals(instanceName)));

        restoreMockConfiguration();
    }

    private RealmSettings getRealm() throws IOException {
        RealmSettings realm = FileConfiguration.get(realmFile, RealmSettings.class);

        ArrayList<String> instances = new ArrayList<>();
        instances.add(instanceName);

        realm.load(instances);
        return realm;
    }

    private void createMockConfiguration(boolean override) throws IOException {
        JsonObject server = new JsonObject()
                .put("enabled", new JsonArray()
                        .add(new JsonObject()
                                .put(ID_REALM, realmName)
                                .put("instances", new JsonArray()
                                        .add(instanceName))));

        JsonObject realm = new JsonObject()
                .put(ID_NAME, realmName);

        JsonObject instance = new JsonObject()
                .put(ID_NAME, instanceName);

        backup = JsonFileStore.readObject(PATH_REALMSERVER);

        new File(instanceDir).mkdirs();
        JsonFileStore.writeObject(realm, realmFile);
        JsonFileStore.writeObject(server, PATH_REALMSERVER);

        if (override) {
            JsonFileStore.writeObject(instance, instanceOverrideFile);
        } else {
            JsonFileStore.writeObject(instance, instanceFile);
        }
    }

    private static void restoreMockConfiguration() {
        JsonFileStore.deleteObject(instanceFile);
        JsonFileStore.deleteObject(instanceOverrideFile);
        JsonFileStore.deleteObject(realmFile);
        JsonFileStore.writeObject(backup, PATH_REALMSERVER);

        new File(instanceDir).delete();
        new File(realmDir).delete();
    }
}
