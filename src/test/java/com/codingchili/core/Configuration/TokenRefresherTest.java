package com.codingchili.core.Configuration;

import com.codingchili.core.Protocols.Util.TokenFactory;
import com.codingchili.core.Realm.Configuration.RealmSettings;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Robin Duda
 *         <p>
 *         Verifies that invalid or expired tokens are regenerated.
 */
public class TokenRefresherTest {
    private static ConfigurationLoader config;

    @BeforeClass
    public static void setUp() {
        config = FileConfiguration.instance();
    }

    @Test
    public void testInvalidRealmTokenRegenerated() {
        for (RealmSettings realm : config.getRealmServerSettings().getRealms()) {
            realm.getAuthentication().getToken().setDomain("something-else");
        }

        verifyInvalidatedRealmTokensAreValidatedOnReload();
    }

    @Test
    public void testInvalidLogTokenRegenerated() {
        for (Configurable configurable : config.getConfigurables()) {
            configurable.getLogserver().getToken().setDomain("something-else");
        }

        verifyInvalidatedLogTokensValidatedOnReload();
    }

    @Test
    public void expiredRealmTokenRegenerated() {
        for (RealmSettings realm : config.getRealmServerSettings().getRealms()) {
            realm.getAuthentication().getToken().setExpiry(0);
        }

        verifyInvalidatedRealmTokensAreValidatedOnReload();
    }

    @Test
    public void expiredLogTokenRegenerated() {
        for (Configurable configurable : config.getConfigurables()) {
            configurable.getLogserver().getToken().setExpiry(0);
        }

        verifyInvalidatedLogTokensValidatedOnReload();
    }

    @Test
    public void loggingTokensAreValid() {
        TokenFactory factory = new TokenFactory(config.getLogSettings().getSecret());

        for (Configurable configurable : config.getConfigurables()) {
            Assert.assertTrue(factory.verifyToken(configurable.getLogserver().getToken()));
        }
    }

    @Test
    public void realmTokensAreValid() {
        TokenFactory factory = new TokenFactory(config.getAuthSettings().getRealmSecret());

        for (RealmSettings realm : config.getRealmServerSettings().getRealms()) {
            Assert.assertTrue(factory.verifyToken(realm.getAuthentication().getToken()));
        }
    }

    @Test
    public void testRegenerateSecrets() {
        String clientSecret = getClientSecret();
        String realmSecret = getRealmSecret();
        String logSecret = getLogSecret();

        TokenRefresher.regenerate();
        reload();

        // Assert they are actually set.
        Assert.assertNotNull(getClientSecret());
        Assert.assertNotNull(getRealmSecret());
        Assert.assertNotNull(getLogSecret());

        // Assert all secrets replaced.
        Assert.assertNotEquals(clientSecret, getClientSecret());
        Assert.assertNotEquals(realmSecret, getRealmSecret());
        Assert.assertNotEquals(logSecret, getLogSecret());
    }

    private void verifyInvalidatedRealmTokensAreValidatedOnReload() {
        try {
            realmTokensAreValid();
            throw new RuntimeException("Test should fail after setting tokens to expired.");
        } catch (AssertionError ignored) {
        }

        reload();
        realmTokensAreValid();
    }

    private void verifyInvalidatedLogTokensValidatedOnReload() {
        try {
            loggingTokensAreValid();
            throw new RuntimeException("Test should fail after setting tokens to expired.");
        } catch (AssertionError ignored) {
        }

        reload();
        loggingTokensAreValid();
    }

    private String getLogSecret() {
        return new String(config.getLogSettings().getSecret());
    }

    private String getRealmSecret() {
        return new String(config.getAuthSettings().getRealmSecret());
    }

    private String getClientSecret() {
        return new String(config.getAuthSettings().getClientSecret());
    }

    private static void reload() {
        FileConfiguration.unload();
        config = FileConfiguration.instance();
    }
}
