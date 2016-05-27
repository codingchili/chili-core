package Authentication.Model;

import Authentication.Controller.*;
import Configuration.AuthServerSettings;
import Configuration.ConfigurationLoader;
import Configuration.FileConfiguration;
import Utilities.DefaultLogger;
import Utilities.Logger;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

/**
 * @author Robin Duda
 */
public class DefaultProvider implements Provider {
    private AuthServerSettings settings = FileConfiguration.instance().getAuthSettings();
    private Vertx vertx;

    public DefaultProvider(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public AsyncAccountStore getAccountStore() {
        return new AccountDB(
                MongoClient.createShared(vertx, new JsonObject()
                        .put("db_name", settings.getDatabase().getName())
                        .put("connection_string", settings.getDatabase().getRemote())));
    }

    @Override
    public ClientProtocol clientProtocol(Access access) {
        ClientRestProtocol protocol = new ClientRestProtocol(this, access);
        vertx.deployVerticle(protocol);
        return protocol;
    }

    @Override
    public RealmProtocol realmProtocol(Access access) {
        RealmWebsocketProtocol protocol = new RealmWebsocketProtocol(this, access);
        vertx.deployVerticle(protocol);
        return protocol;
    }

    @Override
    public Logger getLogger() {
        return new DefaultLogger(vertx, settings.getLogserver());
    }

    @Override
    public ConfigurationLoader getConfig() {
        return FileConfiguration.instance();
    }

    @Override
    public AuthServerSettings getAuthserverSettings() {
        return settings;
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }
}