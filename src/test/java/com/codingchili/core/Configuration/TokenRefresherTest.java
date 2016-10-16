package com.codingchili.core.Configuration;

import com.codingchili.core.Authentication.Configuration.AuthServerSettings;
import com.codingchili.core.Logging.Configuration.LogServerSettings;
import com.codingchili.core.Protocols.Util.TokenFactory;
import com.codingchili.core.Realm.Configuration.EnabledRealm;
import com.codingchili.core.Realm.Configuration.RealmServerSettings;
import com.codingchili.core.Realm.Configuration.RealmSettings;
import com.codingchili.core.Website.Configuration.WebserverSettings;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.codingchili.core.Configuration.Strings.*;

/**
 * @author Robin Duda
 *         <p>
 *         Verifies that invalid or expired tokens are regenerated.
 */
public class TokenRefresherTest {

    @Before
    public void setUp() {
        TokenRefresher.regenerate();
        FileConfiguration.unload();
    }

    @Test
    public void testInvalidRealmTokenRegenerated() {
        for (RealmSettings realm : getRealmSettings()) {
            realm.getAuthentication().getToken().setDomain("something-else");
        }

        verifyInvalidatedRealmTokensAreValidatedOnReload();
    }

    @Test
    public void expiredRealmTokenRegenerated() {
        for (RealmSettings realm : getRealmSettings()) {
            realm.getAuthentication().getToken().setExpiry(0);
        }

        verifyInvalidatedRealmTokensAreValidatedOnReload();
    }


    private void verifyInvalidatedRealmTokensAreValidatedOnReload() {
        try {
            realmTokensAreValid();
            throw new RuntimeException("Test should fail after setting tokens to expired.");
        } catch (AssertionError ignored) {
        }

        FileConfiguration.unload();
        realmTokensAreValid();
    }

    @Test
    public void realmTokensAreValid() {
        TokenFactory factory = new TokenFactory(getAuthServer().getRealmSecret());

        for (RealmSettings realm : getRealmSettings()) {
            Assert.assertTrue(factory.verifyToken(realm.getAuthentication().getToken()));
        }
    }

    @Test
    public void testInvalidLogTokenRegenerated() {
        FileConfiguration.get(PATH_WEBSERVER, WebserverSettings.class);

        FileConfiguration.loaded().stream()
                .filter(configurable -> configurable instanceof Configurable)
                .map(x -> (Configurable) x)
                .forEach(configurable ->
                        configurable.getLogserver().getToken().setDomain("something-else"));

        verifyInvalidatedLogTokensValidatedOnReload();
    }

    @Test
    public void expiredLogTokenRegenerated() {
        FileConfiguration.get(PATH_WEBSERVER, WebserverSettings.class);

        FileConfiguration.loaded().stream()
                .filter(config -> config instanceof Configurable)
                .map(config -> (Configurable) config)
                .forEach(config -> {
                    config.getLogserver().getToken().setExpiry(0);
                    FileConfiguration.save(config);
                });

        FileConfiguration.reload(PATH_WEBSERVER);

        verifyInvalidatedLogTokensValidatedOnReload();
    }

    private void verifyInvalidatedLogTokensValidatedOnReload() {
        try {
            loggingTokensAreValid();
            throw new RuntimeException("Test should fail after setting tokens to expired.");
        } catch (AssertionError ignored) {
        }

        FileConfiguration.unload();
        loggingTokensAreValid();
    }

    @Test
    public void loggingTokensAreValid() {
        TokenFactory factory = new TokenFactory(getLogSecret());

        FileConfiguration.loaded().stream()
                .filter(configurable -> configurable instanceof Configurable)
                .map(configurable -> (Configurable) configurable)
                .forEach(configurable -> {
                    Assert.assertTrue(factory.verifyToken(configurable.getLogserver().getToken()));
                });
    }

    @Test
    public void testRegenerateSecrets() {
        String clientSecret = new String(getClientSecret());
        String realmSecret = new String(getRealmSecret());
        String logSecret = new String(getLogSecret());

        TokenRefresher.regenerate();
        FileConfiguration.unload();

        // Assert they are actually set.
        Assert.assertNotNull(getClientSecret());
        Assert.assertNotNull(getRealmSecret());
        Assert.assertNotNull(getLogSecret());

        // Assert all secrets replaced.
        Assert.assertNotEquals(clientSecret, getClientSecret());
        Assert.assertNotEquals(realmSecret, getRealmSecret());
        Assert.assertNotEquals(logSecret, getLogSecret());
    }

    private byte[] getLogSecret() {
        return getLogserver().getSecret();
    }

    private byte[] getRealmSecret() {
        return getAuthServer().getRealmSecret();
    }

    private byte[] getClientSecret() {
        return getAuthServer().getClientSecret();
    }

    private AuthServerSettings getAuthServer() {
        return FileConfiguration.get(PATH_AUTHSERVER, AuthServerSettings.class);
    }

    private RealmServerSettings getRealmServerSettings() {
        return FileConfiguration.get(PATH_REALMSERVER, RealmServerSettings.class);
    }

    private List<RealmSettings> getRealmSettings() {
        List<RealmSettings> realms = new ArrayList<>();

        for (EnabledRealm enabled : getRealmServerSettings().getEnabled()) {
            realms.add(FileConfiguration.get(enabled.getPath(), RealmSettings.class));
        }

        return realms;
    }

    private LogServerSettings getLogserver() {
        return FileConfiguration.get(PATH_LOGSERVER, LogServerSettings.class);
    }
}
