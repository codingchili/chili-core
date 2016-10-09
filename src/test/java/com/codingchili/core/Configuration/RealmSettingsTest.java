package com.codingchili.core.Configuration;

import com.codingchili.core.Realm.Configuration.RealmServerSettings;
import com.codingchili.core.Realm.Configuration.RealmSettings;
import com.codingchili.core.Realm.Instance.Configuration.InstanceSettings;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static com.codingchili.core.Configuration.Strings.*;

/**
 * @author Robin Duda
 */

@Ignore("RealmSettingsTest: not stable when executed within suite.")
@RunWith(VertxUnitRunner.class)
public class RealmSettingsTest {
    private String realmName = "testRealm";
    private String realmFile = "conf/realm/testRealm.json";
    private String instanceName = "test-instance";
    private String instanceDir = "conf/realm/override/testRealm/instances";
    private String instanceFile = "conf/realm/override/testRealm/instances/" + instanceName + ".json";
    private String realmDir = "conf/realm/override/testRealm";
    private JsonObject backup;

    @Before
    public void setUp() throws IOException {
        createMockConfiguration();
    }

    @After
    public void tearDown() {
        restoreMockConfiguration();
    }

    @Test
    public void testConfigurationOverride(TestContext context) throws IOException {
        RealmServerSettings server = FileConfiguration.instance().getRealmServerSettings();

        RealmSettings realm = server.getRealms().get(0);
        context.assertEquals(1, realm.getInstance().size());
        context.assertEquals(realmName, realm.getName());

        InstanceSettings instance = realm.getInstance().get(0);
        context.assertEquals(instance.getName(), instanceName);
    }


    @Test(expected = AssertionError.class)
    public void testConfigurationNotOverridden(TestContext context) throws IOException {
        restoreMockConfiguration();
        testConfigurationOverride(context);
        createMockConfiguration();
    }

    private void createMockConfiguration() throws IOException {
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
        JsonFileStore.writeObject(instance, instanceFile);
    }

    private void restoreMockConfiguration() {
        JsonFileStore.deleteObject(instanceFile);
        JsonFileStore.deleteObject(realmFile);
        JsonFileStore.writeObject(backup, PATH_REALMSERVER);

        new File(instanceDir).delete();
        new File(realmDir).delete();
    }
}
