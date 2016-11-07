package com.codingchili.services;

import org.junit.*;

import java.util.ArrayList;
import java.util.List;

import com.codingchili.core.Files.Configurations;
import com.codingchili.core.Security.TokenFactory;

import com.codingchili.services.Authentication.Configuration.AuthServerSettings;
import com.codingchili.services.Logging.Configuration.LogServerSettings;
import com.codingchili.services.Realm.Configuration.*;

import static com.codingchili.services.Authentication.Configuration.AuthServerSettings.PATH_AUTHSERVER;
import static com.codingchili.services.Logging.Configuration.LogServerSettings.PATH_LOGSERVER;
import static com.codingchili.services.Realm.Configuration.RealmServerSettings.PATH_REALMSERVER;


/**
 * @author Robin Duda
 *         <p>
 *         Verifies that invalid or expired tokens are regenerated.
 */
@Ignore
public class TokenRefresherTest {

    @Before
    public void setUp() {
        //TokenRefresher.regenerate();
        Configurations.unload();
    }

    @Test
    public void testInvalidRealmTokenRegenerated() {
        for (RealmSettings realm : getRealmSettings()) {
            realm.getAuthentication().setDomain("something-else");
        }

        verifyInvalidatedRealmTokensAreValidatedOnReload();
    }

    @Test
    public void expiredRealmTokenRegenerated() {
        for (RealmSettings realm : getRealmSettings()) {
            realm.getAuthentication().setExpiry(0);
        }

        verifyInvalidatedRealmTokensAreValidatedOnReload();
    }


    private void verifyInvalidatedRealmTokensAreValidatedOnReload() {
        try {
            realmTokensAreValid();
            throw new RuntimeException("Test should fail after setting tokens to expired.");
        } catch (AssertionError ignored) {
        }

        Configurations.unload();
        realmTokensAreValid();
    }

    @Test
    public void realmTokensAreValid() {
        TokenFactory factory = new TokenFactory(getAuthServer().getRealmSecret());

        for (RealmSettings realm : getRealmSettings()) {
            Assert.assertTrue(factory.verifyToken(realm.getAuthentication()));
        }
    }

    @Test
    public void testRegenerateSecrets() {
        String clientSecret = new String(getClientSecret());
        String realmSecret = new String(getRealmSecret());
        String logSecret = new String(getLogSecret());

       // TokenRefresher.regenerate();
        Configurations.unload();

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
        return Configurations.get(PATH_AUTHSERVER, AuthServerSettings.class);
    }

    private RealmServerSettings getRealmServerSettings() {
        return Configurations.get(PATH_REALMSERVER, RealmServerSettings.class);
    }

    private List<RealmSettings> getRealmSettings() {
        List<RealmSettings> realms = new ArrayList<>();

        for (EnabledRealm enabled : getRealmServerSettings().getEnabled()) {
            realms.add(Configurations.get(enabled.getPath(), RealmSettings.class));
        }

        return realms;
    }

    private LogServerSettings getLogserver() {
        return Configurations.get(PATH_LOGSERVER, LogServerSettings.class);
    }
}
