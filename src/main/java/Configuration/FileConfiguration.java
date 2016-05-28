package Configuration;

import Utilities.*;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;

import static Configuration.GameServerSettings.REALM_PATH;

/**
 * @author Robin Duda
 *         Handles loading and parsing of the configuration files.
 */
public class FileConfiguration implements ConfigurationLoader {
    private static final int SECRET_SIZE = 64;
    private static ConfigurationLoader instance = new FileConfiguration();
    private AuthServerSettings authentication;
    private LogServerSettings logging;
    private GameServerSettings gameserver;
    private WebServerSettings webserver;

    private FileConfiguration() {
        try {
            authentication = (AuthServerSettings) load(AuthServerSettings.AUTHSERVER_PATH, AuthServerSettings.class);
            gameserver = (GameServerSettings) load(GameServerSettings.GAMESERVER_PATH, GameServerSettings.class);
            logging = (LogServerSettings) load(LogServerSettings.LOGSERVER_PATH, LogServerSettings.class);
            webserver = (WebServerSettings) load(WebServerSettings.WEBSERVER_PATH, WebServerSettings.class);

            loadRealms(gameserver);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadRealms(GameServerSettings gameserver) throws IOException {
        ArrayList<RealmSettings> realms = new ArrayList<>();
        ArrayList<JsonObject> configurations = JsonFileStore.readDirectoryObjects(REALM_PATH);

        for (JsonObject configuration : configurations)
            realms.add((RealmSettings) Serializer.unpack(configuration, RealmSettings.class));

        gameserver.setRealms(realms);
    }

    public static ConfigurationLoader instance() {
        return instance;
    }

    private Object load(String path, Class clazz) throws IOException {
        return Serializer.unpack(JsonFileStore.readObject(path), clazz);
    }

    @Override
    public WebServerSettings getWebServerSettings() {
        return webserver;
    }

    @Override
    public GameServerSettings getGameServerSettings() {
        return gameserver;
    }

    @Override
    public LogServerSettings getLogSettings() {
        return logging;
    }

    @Override
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
        ArrayList<JsonObject> realms = JsonFileStore.readDirectoryObjects("conf/realmName/");

        for (JsonObject realm : realms) {
            JsonObject remote = realm.getJsonObject("authentication");
            String name = realm.getString("name");
            remote.put("token", Serializer.json(new Token(factory, name)));
            realm.put("authentication", remote);
            JsonFileStore.writeObject(realm, getRealmPath(name));
        }
    }

    private String getRealmPath(String name) {
        return REALM_PATH + name + ".json";
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
