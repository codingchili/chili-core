package Authentication.Model;

import Authentication.Controller.ClientRequest;
import Authentication.Controller.PacketHandler;
import Authentication.Controller.Protocol;
import Authentication.Controller.RealmRequest;
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
    private Protocol<PacketHandler<ClientRequest>> clientProtocol = new Protocol<>();
    private Protocol<PacketHandler<RealmRequest>> realmProtocol = new Protocol<>();
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
    public Protocol<PacketHandler<ClientRequest>> clientProtocol() {
        return clientProtocol;
    }

    @Override
    public Protocol<PacketHandler<RealmRequest>> realmProtocol() {
        return realmProtocol;
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