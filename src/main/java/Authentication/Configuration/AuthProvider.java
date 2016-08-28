package Authentication.Configuration;

import Authentication.Controller.ClientRequest;
import Authentication.Controller.RealmRequest;
import Authentication.Model.AccountDB;
import Authentication.Model.AsyncAccountStore;
import Configuration.ConfigurationLoader;
import Configuration.FileConfiguration;
import Configuration.Provider;
import Logging.Model.DefaultLogger;
import Logging.Model.Logger;
import Protocols.PacketHandler;
import Protocols.Protocol;
import io.vertx.core.Vertx;
import io.vertx.ext.mongo.MongoClient;

/**
 * @author Robin Duda
 */
public class AuthProvider implements Provider {
    private Protocol<PacketHandler<ClientRequest>> clientProtocol = new Protocol<>();
    private Protocol<PacketHandler<RealmRequest>> realmProtocol = new Protocol<>();
    private AuthServerSettings settings;
    private ConfigurationLoader loader;
    private Vertx vertx;

    public AuthProvider(Vertx vertx) {
        this.vertx = vertx;
        this.loader = FileConfiguration.instance();
        this.settings = loader.getAuthSettings();
    }

    public AsyncAccountStore getAccountStore() {
        MongoClient client = MongoClient.createShared(vertx, settings.getDatabase().toJson());
        return new AccountDB(client);
    }

    public DatabaseSettings getDatabase() {
        return settings.getDatabase();
    }

    public Protocol<PacketHandler<ClientRequest>> clientProtocol() {
        return clientProtocol;
    }

    public Protocol<PacketHandler<RealmRequest>> realmProtocol() {
        return realmProtocol;
    }

    @Override
    public Logger getLogger() {
        return new DefaultLogger(vertx, settings.getLogserver());
    }

    @Override
    public ConfigurationLoader getConfig() {
        return loader;
    }

    public AuthServerSettings getAuthserverSettings() {
        return settings;
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }
}