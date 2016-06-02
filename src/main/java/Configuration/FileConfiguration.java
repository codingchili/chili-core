package Configuration;

import Configuration.Authserver.AuthServerSettings;
import Configuration.Gameserver.GameServerSettings;
import Configuration.Gameserver.RealmSettings;
import Configuration.Logserver.LogServerSettings;
import Configuration.Webserver.MetaServerSettings;
import Protocols.Serializer;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.util.ArrayList;

import static Configuration.Authserver.AuthServerSettings.AUTHSERVER_PATH;
import static Configuration.Gameserver.GameServerSettings.GAMESERVER_PATH;
import static Configuration.Gameserver.GameServerSettings.REALM_PATH;
import static Configuration.Logserver.LogServerSettings.LOGSERVER_PATH;
import static Configuration.Webserver.MetaServerSettings.METASERVER_PATH;


/**
 * @author Robin Duda
 *         Handles loading and parsing of the configuration files.
 */
public class FileConfiguration implements ConfigurationLoader {
    private static ConfigurationLoader instance;
    private AuthServerSettings authentication;
    private LogServerSettings logging;
    private GameServerSettings gameserver;
    private MetaServerSettings webserver;

    private FileConfiguration() {
        try {
            authentication = (AuthServerSettings) load(AUTHSERVER_PATH, AuthServerSettings.class);
            gameserver = (GameServerSettings) load(GAMESERVER_PATH, GameServerSettings.class);
            logging = (LogServerSettings) load(LOGSERVER_PATH, LogServerSettings.class);
            webserver = (MetaServerSettings) load(METASERVER_PATH, MetaServerSettings.class);
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

    public static synchronized ConfigurationLoader instance() {
        if (instance == null) {
            instance = new FileConfiguration();
            TokenRefresher.refresh();
        }
        return instance;
    }

    private Object load(String path, Class clazz) throws IOException {
        return Serializer.unpack(JsonFileStore.readObject(path), clazz);
    }

    @Override
    public MetaServerSettings getMetaServerSettings() {
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

    void save() {
        Configurable[] configurables = {authentication, logging, gameserver, webserver};

        for (Configurable configurable : configurables) {
            JsonFileStore.writeObject(Serializer.json(configurable), getConfigPath(configurable));
        }

        for (RealmSettings realm : gameserver.getRealms()) {
            JsonObject json = Serializer.json(realm);

            json.remove("afflictions");
            json.remove("classes");

            JsonFileStore.writeObject(json, getRealmPath(realm));
        }
    }

    private String getRealmPath(RealmSettings realm) {
        return REALM_PATH + realm.getName() + ".json";
    }

    private String getConfigPath(Configurable configurable) {
        return "conf/system/" + configurable.getName() + ".json";
    }
}
