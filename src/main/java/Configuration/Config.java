package Configuration;

import Utilities.*;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;

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

    void generateAuthSecret() {
        authentication.setRealmSecret(getRandomString());
        authentication.setClientSecret(getRandomString());
    }

    private byte[] getRandomString() {
        SecureRandom random = new SecureRandom();
        byte[] secret = new byte[SECRET_SIZE];
        random.nextBytes(secret);
        return secret;
    }

    void generateLoggingSecret() {
        logging.setSecret(getRandomString());
    }

    void generateRealmTokens() throws IOException {
        TokenFactory factory = new TokenFactory(authentication.getRealmSecret());
        ArrayList<JsonObject> realms = JsonFileStore.readDirectoryObjects("conf/realm/");

        for (JsonObject realm : realms) {
            JsonObject remote = realm.getJsonObject("authentication");
            String name = realm.getString("name");
            remote.put("token", Serializer.json(new Token(factory, name)));
            realm.put("authentication", remote);
            JsonFileStore.writeObject(realm, getRealmPath(name));
        }
    }

    private String getRealmPath(String name) {
        return GameServerSettings.REALM_PATH + name + ".json";
    }

    void generateLoggingTokens() {
        Configurable[] classes = {authentication, logging, webserver, gameserver};
        TokenFactory factory = new TokenFactory(logging.getSecret());

        for (Configurable configurable : classes) {
            JsonObject configuration = Serializer.json(configurable);

            configuration.getJsonObject("logserver")
                    .put("token", Serializer.json(new Token(factory, configurable.getName())));

            JsonFileStore.writeObject(configuration, getConfigPath(configurable.getName()));
        }
    }

    private String getConfigPath(String name) {
        return "conf/system/" + name + ".json";
    }
}
