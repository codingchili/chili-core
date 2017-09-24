package com.codingchili.realmregistry;

import com.codingchili.realmregistry.configuration.RealmRegistrySettings;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Robin Duda
 */
@RunWith(VertxUnitRunner.class)
public class ConfigurationTest {

    @Test
    public void testTrustedRealm() {
        RealmRegistrySettings auth = new RealmRegistrySettings();

        auth.getRealms().add("realmName");

        Assert.assertTrue(auth.isTrustedRealm("realmName"));
    }

    @Test
    public void testNotTrustedRealm() {
        Assert.assertFalse(new RealmRegistrySettings().isTrustedRealm("not trusted"));
    }

}
