package com.codingchili.core.Configuration;

import com.codingchili.core.Authentication.Configuration.AuthServerSettings;
import com.codingchili.core.Logging.Configuration.LogServerSettings;
import com.codingchili.core.Patching.Configuration.PatchServerSettings;
import com.codingchili.core.Protocols.Util.Token;
import com.codingchili.core.Protocols.Util.TokenFactory;
import com.codingchili.core.Realm.Configuration.EnabledRealm;
import com.codingchili.core.Realm.Configuration.RealmServerSettings;
import com.codingchili.core.Realm.Configuration.RealmSettings;
import com.codingchili.core.Routing.Configuration.RoutingSettings;
import com.codingchili.core.Website.Configuration.WebserverSettings;

import java.security.SecureRandom;

import static com.codingchili.core.Configuration.Strings.*;

/**
 * @author Robin Duda
 *         Generates new tokens and optionally secrets and writes them to the configuration files.
 */
class TokenRefresher {
    private static final int SECRET_SIZE = 64;
    private TokenFactory loggerTokens;
    private TokenFactory realmTokens;

    public static void main(String[] args) {
        refresh();
    }

    static void refresh() {
        new TokenRefresher().generate(false);
    }

    static void regenerate() {
        new TokenRefresher().generate(true);
    }

    private void generate(boolean replaceSecrets) {
        AuthServerSettings authServerSettings = FileConfiguration.get(PATH_AUTHSERVER, AuthServerSettings.class);
        LogServerSettings logServerSettings = FileConfiguration.get(PATH_LOGSERVER, LogServerSettings.class);

        generateAuthSecrets(authServerSettings, replaceSecrets);
        generateLoggingSecret(logServerSettings, replaceSecrets);

        generateLoggingTokens(new Configurable[]{
                FileConfiguration.get(PATH_AUTHSERVER, AuthServerSettings.class),
                FileConfiguration.get(PATH_REALMSERVER, RealmServerSettings.class),
                FileConfiguration.get(PATH_LOGSERVER, LogServerSettings.class),
                FileConfiguration.get(PATH_PATCHSERVER, PatchServerSettings.class),
                FileConfiguration.get(PATH_WEBSERVER, WebserverSettings.class),
                FileConfiguration.get(PATH_ROUTING, RoutingSettings.class)
        });

        generateRealmTokens(FileConfiguration.get(PATH_REALMSERVER, RealmServerSettings.class));

        FileConfiguration.<LoadableConfigurable>loaded().stream()
                .forEach(FileConfiguration::save);
    }

    private void generateAuthSecrets(AuthServerSettings authserver, boolean replace) {
        if (replace || authserver.getRealmSecret() == null) {
            authserver.setRealmSecret(getRandomString());
        }

        if (replace || authserver.getClientSecret() == null) {
            authserver.setClientSecret(getRandomString());
        }

        realmTokens = new TokenFactory(authserver.getRealmSecret());
    }

    private byte[] getRandomString() {
        SecureRandom random = new SecureRandom();
        byte[] secret = new byte[SECRET_SIZE];
        random.nextBytes(secret);
        return secret;
    }

    private void generateLoggingSecret(LogServerSettings logserver, boolean replace) {
        if (replace || logserver.getSecret() == null) {
            logserver.setSecret(getRandomString());
        }

        loggerTokens = new TokenFactory(logserver.getSecret());
    }

    private void generateLoggingTokens(Configurable[] configurable) {
        for (Configurable logger : configurable) {
            RemoteAuthentication remote = logger.getLogserver();

            if (remote != null) {
                if (remote.getToken() == null || invalidToken(remote.getToken(), loggerTokens)) {
                    remote.setToken(new Token(loggerTokens, remote.getSystem() + Strings.LOG_AT + remote.getHost()));
                }
            }
        }
    }

    private boolean invalidToken(Token token, TokenFactory factory) {
        return !factory.verifyToken(token);
    }

    private void generateRealmTokens(RealmServerSettings gameserver) {
        for (EnabledRealm enabled : gameserver.getEnabled()) {
            RealmSettings realm = FileConfiguration.get(enabled.getPath(), RealmSettings.class);
            RemoteAuthentication remote = realm.getAuthentication();

            if (remote.getToken() == null || invalidToken(remote.getToken(), realmTokens)) {
                remote.setToken(new Token(realmTokens, realm.getName()));
            }
        }
    }
}
