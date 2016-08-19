package Configuration;

import Authentication.Configuration.AuthServerSettings;
import Realm.Configuration.GameServerSettings;
import Realm.Configuration.RealmSettings;
import Logging.Configuration.LogServerSettings;
import Patching.Configuration.PatchServerSettings;
import Protocols.Serializer;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.util.ArrayList;


/**
 * @author Robin Duda
 *         Handles loading and parsing of the configuration files.
 */
public class FileConfiguration implements ConfigurationLoader {
    private static ConfigurationLoader instance;
    private AuthServerSettings authentication;
    private LogServerSettings logserver;
    private GameServerSettings gameserver;
    private PatchServerSettings patchserver;

    private FileConfiguration() throws IOException {
        authentication = (AuthServerSettings) load(Strings.PATH_AUTHSERVER, AuthServerSettings.class);
        gameserver = (GameServerSettings) load(Strings.PATH_GAMESERVER, GameServerSettings.class);
        logserver = (LogServerSettings) load(Strings.PATH_LOGSERVER, LogServerSettings.class);
        patchserver = loadPatchSettings();
        loadRealms(gameserver);
    }

    private static Object load(String path, Class clazz) {
        try {
            return Serializer.unpack(JsonFileStore.readObject(path), clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Expose the loader for loading settings during runtime.
     *
     * @return PatchServerSettings instantiated from JSON at #Strings.PATH_PATCHSERVER
     */
    public static PatchServerSettings loadPatchSettings() {
        return (PatchServerSettings) load(Strings.PATH_PATCHSERVER, PatchServerSettings.class);
    }

    private void loadRealms(GameServerSettings gameserver) {
        ArrayList<RealmSettings> realms = new ArrayList<>();
        try {
            ArrayList<JsonObject> configurations = JsonFileStore.readDirectoryObjects(Strings.PATH_REALM);

            for (JsonObject configuration : configurations)
                realms.add((RealmSettings) Serializer.unpack(configuration, RealmSettings.class));

            gameserver.setRealms(realms);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized ConfigurationLoader instance() {
        if (instance == null) {
            try {
                instance = new FileConfiguration();
            } catch (IOException e) {
                throw new RuntimeException(Strings.ERROR_LOADING_CONFIGURATION);
            }

            if (((FileConfiguration) instance).containsAllSettings())
                TokenRefresher.refresh();
        }
        return instance;
    }

    private boolean containsAllSettings() {
        return (authentication != null && logserver != null && gameserver != null && patchserver != null);
    }

    @Override
    public PatchServerSettings getPatchServerSettings() {
        return patchserver;
    }

    @Override
    public GameServerSettings getGameServerSettings() {
        return gameserver;
    }

    @Override
    public LogServerSettings getLogSettings() {
        return logserver;
    }

    @Override
    public AuthServerSettings getAuthSettings() {
        return authentication;
    }

    void save() {
        Configurable[] configurables = {authentication, logserver, gameserver, patchserver};

        for (Configurable configurable : configurables) {
            JsonFileStore.writeObject(Serializer.json(configurable), getConfigPath(configurable));
        }

        for (RealmSettings realm : gameserver.getRealms()) {
            JsonObject json = Serializer.json(realm);

            json.remove(Strings.GAME_AFFLICTIONS);
            json.remove(Strings.GAME_CLASSES);

            JsonFileStore.writeObject(json, getRealmPath(realm));
        }
    }

    private String getRealmPath(RealmSettings realm) {
        return Strings.PATH_REALM + realm.getName() + Strings.EXT_JSON;
    }

    private String getConfigPath(Configurable configurable) {
        return Strings.DIR_SYSTEM + configurable.getName() + Strings.EXT_JSON;
    }
}
