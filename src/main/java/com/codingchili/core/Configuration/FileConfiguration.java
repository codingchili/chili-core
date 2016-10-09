package com.codingchili.core.Configuration;

import com.codingchili.core.Authentication.Configuration.AuthServerSettings;
import com.codingchili.core.Logging.Configuration.LogServerSettings;
import com.codingchili.core.Patching.Configuration.PatchServerSettings;
import com.codingchili.core.Protocols.Util.Serializer;
import com.codingchili.core.Realm.Configuration.EnabledRealm;
import com.codingchili.core.Realm.Configuration.RealmServerSettings;
import com.codingchili.core.Realm.Configuration.RealmSettings;
import com.codingchili.core.Routing.Configuration.RoutingSettings;
import com.codingchili.core.Website.Configuration.WebserverSettings;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.util.ArrayList;

import static com.codingchili.core.Configuration.Strings.EXT_JSON;
import static com.codingchili.core.Configuration.Strings.PATH_REALM;


/**
 * @author Robin Duda
 *         Handles loading and parsing of the configuration files.
 */
public class FileConfiguration implements ConfigurationLoader {
    private static ConfigurationLoader instance;
    private AuthServerSettings authentication;
    private LogServerSettings logserver;
    private RealmServerSettings realmserver;
    private PatchServerSettings patchserver;
    private WebserverSettings webserver;
    private RoutingSettings routing;
    private LauncherSettings launcher;
    private DeploySettings deploy;
    private VertxSettings vertxSettings;

    private FileConfiguration() throws IOException {
        launcher = Serializer.unpack(JsonFileStore.readObject(Strings.PATH_LAUNCHER), LauncherSettings.class);
        deploy = Serializer.unpack(JsonFileStore.readObject(Strings.PATH_DEPLOY), DeploySettings.class);
        authentication = Serializer.unpack(JsonFileStore.readObject(Strings.PATH_AUTHSERVER), AuthServerSettings.class);
        realmserver = Serializer.unpack(JsonFileStore.readObject(Strings.PATH_REALMSERVER), RealmServerSettings.class);
        logserver = Serializer.unpack(JsonFileStore.readObject(Strings.PATH_LOGSERVER), LogServerSettings.class);
        webserver = Serializer.unpack(JsonFileStore.readObject(Strings.PATH_WEBSERVER), WebserverSettings.class);
        routing = Serializer.unpack(JsonFileStore.readObject(Strings.PATH_ROUTING), RoutingSettings.class);
        vertxSettings = Serializer.unpack(JsonFileStore.readObject(Strings.PATH_VERTX), VertxSettings.class);
        patchserver = loadPatchSettings();
        loadRealms(realmserver);
    }

    /**
     * Expose the loader for loading settings during runtime.
     *
     * @return PatchServerSettings instantiated from JSON at #Strings.PATH_PATCHSERVER
     */
    public static PatchServerSettings loadPatchSettings() throws IOException {
        return Serializer.unpack(JsonFileStore.readObject(Strings.PATH_PATCHSERVER), PatchServerSettings.class);
    }

    private void loadRealms(RealmServerSettings realmserver) {
        ArrayList<RealmSettings> realms = new ArrayList<>();
        try {
            for (EnabledRealm enabled : realmserver.getEnabled()) {
                JsonObject configuration = JsonFileStore.readObject(getRealmPath(enabled.getRealm()));
                RealmSettings realm = (Serializer.unpack(configuration, RealmSettings.class));
                realm.load(enabled);
                realms.add(realm);
            }

            realmserver.setRealms(realms);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized ConfigurationLoader instance() {
        if (instance == null) {
            try {
                instance = new FileConfiguration();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            TokenRefresher.refresh();
        }
        return instance;
    }

    @Override
    public VertxSettings getVertxSettings() {
        return vertxSettings;
    }

    @Override
    public PatchServerSettings getPatchServerSettings() {
        return patchserver;
    }

    @Override
    public RealmServerSettings getRealmServerSettings() {
        return realmserver;
    }

    @Override
    public LogServerSettings getLogSettings() {
        return logserver;
    }

    @Override
    public AuthServerSettings getAuthSettings() {
        return authentication;
    }

    @Override
    public WebserverSettings getWebsiteSettings() {
        return webserver;
    }

    @Override
    public RoutingSettings getRoutingSettings() {
        return routing;
    }

    @Override
    public LauncherSettings getLauncherSettings() {
        return launcher;
    }

    @Override
    public DeploySettings getDeploySettings() {
        return deploy;
    }

    void save() {
        Configurable[] configurables = {authentication, logserver, realmserver, patchserver, webserver, routing};

        for (Configurable configurable : configurables) {
            JsonFileStore.writeObject(Serializer.json(configurable), getConfigPath(configurable));
        }

        for (RealmSettings realm : realmserver.getRealms()) {
            JsonObject json = Serializer.json(realm);

            json.remove(Strings.GAME_AFFLICTIONS);
            json.remove(Strings.GAME_CLASSES);

            JsonFileStore.writeObject(json, getRealmPath(realm));
        }
    }

    private String getRealmPath(RealmSettings realm) {
        return getRealmPath(realm.getName());
    }

    private String getRealmPath(String name) {
        return PATH_REALM + name + Strings.EXT_JSON;
    }

    private String getConfigPath(Configurable configurable) {
        return configurable.getPath();
    }
}
