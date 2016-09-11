package Authentication.Configuration;

import Authentication.Controller.ClientRequest;
import Authentication.Controller.RealmRequest;
import Authentication.Model.AsyncAccountStore;
import Authentication.Model.AsyncRealmStore;
import Authentication.Model.HazelAccountDB;
import Authentication.Model.HazelRealmDB;
import Configuration.FileConfiguration;
import Configuration.Provider;
import Configuration.Strings;
import Logging.Model.DefaultLogger;
import Logging.Model.Logger;
import Protocols.PacketHandler;
import Protocols.Protocol;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 */
public class AuthProvider implements Provider {
    private Protocol<PacketHandler<ClientRequest>> clientProtocol = new Protocol<>();
    private Protocol<PacketHandler<RealmRequest>> realmProtocol = new Protocol<>();
    private AsyncAccountStore accounts;
    private AsyncRealmStore realms;
    private AuthServerSettings settings;
    private Logger logger;

    public AuthProvider() {
    }

    public AuthProvider(AsyncRealmStore realms, AsyncAccountStore accounts, Vertx vertx) {
        this.realms = realms;
        this.accounts = accounts;
        this.settings = FileConfiguration.instance().getAuthSettings();
        this.logger = new DefaultLogger(vertx, settings.getLogserver());
    }

    public static void create(Future<AuthProvider> future, Vertx vertx) {
        if (vertx.isClustered()) {
            Future<AsyncRealmStore> realmFuture = Future.future();
            Future<AsyncAccountStore> accountFuture = Future.future();

            CompositeFuture.all(realmFuture, accountFuture).setHandler(initialization -> {
                AsyncRealmStore realms = (AsyncRealmStore) initialization.result().list().get(0);
                AsyncAccountStore accounts = (AsyncAccountStore) initialization.result().list().get(1);

                future.complete(new AuthProvider(realms, accounts, vertx));
            });

            HazelAccountDB.create(accountFuture, vertx);
            HazelRealmDB.create(realmFuture, vertx);
        } else {
            throw new RuntimeException(Strings.ERROR_CLUSTERING_REQUIRED);
        }
    }

    public AsyncRealmStore getRealmStore() {
        return realms;
    }

    public AsyncAccountStore getAccountStore() {
        return accounts;
    }

    public Protocol<PacketHandler<ClientRequest>> clientProtocol() {
        return clientProtocol;
    }

    public Protocol<PacketHandler<RealmRequest>> realmProtocol() {
        return realmProtocol;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    public AuthServerSettings getAuthserverSettings() {
        return settings;
    }
}