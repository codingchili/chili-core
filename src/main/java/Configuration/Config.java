package Configuration;

import Utilities.*;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.security.SecureRandom;

/**
 * Created by Robin on 2016-04-07.
 */

public class Config {
    private static final int SECRET_SIZE = 64;
    private static Config instance = new Config();
    private AuthServerSettings authentication;
    private LogServerSettings logging;
    private GameServerSettings gameserver;
    private WebServerSettings webserver;

    private Config() {
        try {
            authentication = (AuthServerSettings) Serializer.unpack(
                    JsonFileStore.readObject(AuthServerSettings.AUTHSERVER_PATH), AuthServerSettings.class);

            gameserver = (GameServerSettings) Serializer.unpack(
                    JsonFileStore.readObject(GameServerSettings.GAMESERVER_PATH), GameServerSettings.class);

            logging = (LogServerSettings) Serializer.unpack(
                    JsonFileStore.readObject(LogServerSettings.LOGSERVER_PATH), LogServerSettings.class);

            webserver = (WebServerSettings) Serializer.unpack(
                    JsonFileStore.readObject(WebServerSettings.WEBSERVER_PATH), WebServerSettings.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Config instance() {
        return instance;
    }

    protected static RemoteAuthentication LoadLogToken(JsonObject configuration) {
        return (RemoteAuthentication) Serializer.unpack(configuration.getJsonObject("logserver"), RemoteAuthentication.class);
    }

    public static class Address {
        public final static String LOGS = "LOGGING";
    }

    public WebServerSettings getWebServerSettings() {
        return webserver;
    }

    public GameServerSettings getGameServerSettings() {
        return gameserver;
    }

    public LogServerSettings getLogSettings() {
        return logging;
    }

    public AuthServerSettings getAuthSettings() {
        return authentication;
    }

    public void generateAuthSecret() {
        authentication.setRealmSecret(getRandomString());
        authentication.setClientSecret(getRandomString());
    }

    private byte[] getRandomString() {
        SecureRandom random = new SecureRandom();
        byte[] secret = new byte[SECRET_SIZE];
        random.nextBytes(secret);
        return secret;
    }

    public void generateLoggingSecret() {
        logging.setSecret(getRandomString());
    }

    public void generateRealmTokens() {
        TokenFactory factory = new TokenFactory(authentication.getRealmSecret());

        for (RealmSettings realm : gameserver.getRealms()) {
            RemoteAuthentication remote = realm.getAuthentication();
            remote.setToken(new Token(factory, realm.getName()));
            realm.setAuthentication(remote);
            JsonFileStore.writeObject(Serializer.json(realm), getRealmPath(realm));
        }
    }

    private String getRealmPath(RealmSettings realm) {
        return GameServerSettings.REALM_PATH + realm.getName() + ".json";
    }

    public void generateLoggingTokens() {
        Configurable[] classes = {authentication, logging, webserver, gameserver};
        TokenFactory factory = new TokenFactory(logging.getSecret());

        for (Configurable configurable : classes) {
            JsonObject configuration = Serializer.json(configurable);

            configuration.getJsonObject("logserver")
                    .put("token", Serializer.json(new Token(factory, configurable.getName())));

            JsonFileStore.writeObject(configuration, getConfigPath(configurable.getName()));
        }
    }

    public String getConfigPath(String name) {
        return "conf/system/" + name + ".json";
    }
}
