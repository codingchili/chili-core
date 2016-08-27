package Configuration;

import Authentication.Configuration.AuthServerSettings;
import Realm.Configuration.RealmServerSettings;
import Realm.Configuration.RealmSettings;
import Logging.Configuration.LogServerSettings;
import Protocols.Authorization.Token;
import Protocols.Authorization.TokenFactory;

import java.security.SecureRandom;

/**
 * @author Robin Duda
 *         Generates new tokens and optionally secrets and writes them to the configuration files.
 */
class TokenRefresher {
    private static final int SECRET_SIZE = 64;
    private TokenFactory loggerTokens;
    private TokenFactory realmTokens;

    static void refresh() {
        new TokenRefresher().generate();
    }

    private void generate() {
        FileConfiguration configuration = (FileConfiguration) FileConfiguration.instance();

        generateAuthSecrets(configuration.getAuthSettings());
        generateLoggingSecret(configuration.getLogSettings());

        generateLoggingTokens(new Configurable[]{
                configuration.getAuthSettings(),
                configuration.getGameServerSettings(),
                configuration.getLogSettings(),
                configuration.getPatchServerSettings(),
                configuration.getWebsiteSettings()
        });

        generateRealmTokens(configuration.getGameServerSettings());

        configuration.save();
    }

    private void generateAuthSecrets(AuthServerSettings authserver) {
        if (authserver.getRealmSecret() == null) {
            authserver.setRealmSecret(getRandomString());
        }

        if (authserver.getClientSecret() == null) {
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

    private void generateLoggingSecret(LogServerSettings logserver) {
        if (logserver.getSecret() == null) {
            logserver.setSecret(getRandomString());
        }

        loggerTokens = new TokenFactory(logserver.getSecret());
    }

    private void generateLoggingTokens(Configurable[] configurable) {
        for (Configurable logger : configurable) {
            RemoteAuthentication remote = logger.getLogserver();

            if (remote.getToken() == null || !verifyToken(remote.getToken(), loggerTokens)) {
                remote.setToken(new Token(loggerTokens, remote.getSystem() + Strings.LOG_AT + remote.getHost()));
            }
        }
    }

    private boolean verifyToken(Token token, TokenFactory factory) {
        return factory.verifyToken(token);
    }

    private void generateRealmTokens(RealmServerSettings gameserver) {
        for (RealmSettings realm : gameserver.getRealms()) {
            RemoteAuthentication remote = realm.getAuthentication();

            if (remote.getToken() == null || !verifyToken(remote.getToken(), realmTokens)) {
                remote.setToken(new Token(realmTokens, realm.getName()));
            }
        }
    }
}
