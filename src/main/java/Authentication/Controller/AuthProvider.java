package Authentication.Controller;

import Authentication.Model.AccountDB;
import Authentication.Model.AsyncAccountStore;
import Configuration.Authserver.AuthServerSettings;
import Configuration.ConfigurationLoader;
import Configuration.FileConfiguration;
import Configuration.Provider;
import Protocols.PacketHandler;
import Protocols.Protocol;
import Utilities.DefaultLogger;
import Utilities.Logger;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

/**
 * @author Robin Duda
 */
public class AuthProvider implements Provider {
    private Protocol<PacketHandler<ClientRequest>> clientProtocol = new Protocol<>();
    private Protocol<PacketHandler<RealmRequest>> realmProtocol = new Protocol<>();
    private Vertx vertx;

    public AuthProvider(Vertx vertx) {
        this.vertx = vertx;
    }

    public AsyncAccountStore getAccountStore() {
        return new AccountDB(
                MongoClient.createShared(vertx, new JsonObject()
                        .put("db_name", settings().getDatabase().getName())
                        .put("connection_string", settings().getDatabase().getRemote())));
    }

    public Protocol<PacketHandler<ClientRequest>> clientProtocol() {
        return clientProtocol;
    }

    public Protocol<PacketHandler<RealmRequest>> realmProtocol() {
        return realmProtocol;
    }

    @Override
    public Logger getLogger() {
        return new DefaultLogger(vertx, settings().getLogserver());
    }

    @Override
    public ConfigurationLoader getConfig() {
        return FileConfiguration.instance();
    }

    public AuthServerSettings getAuthserverSettings() {
        return FileConfiguration.instance().getAuthSettings();
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    private AuthServerSettings settings() {
        return FileConfiguration.instance().getAuthSettings();
    }
}