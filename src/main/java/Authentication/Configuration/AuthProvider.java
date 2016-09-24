package Authentication.Configuration;

import Authentication.Model.AsyncAccountStore;
import Authentication.Model.AsyncRealmStore;
import Authentication.Model.HazelAccountDB;
import Authentication.Model.HazelRealmDB;
import Configuration.FileConfiguration;
import Configuration.Provider;
import Configuration.Strings;
import Logging.Model.DefaultLogger;
import Logging.Model.Logger;
import Protocols.Authorization.TokenFactory;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 */
public class AuthProvider implements Provider {
    private AsyncAccountStore accounts;
    private AsyncRealmStore realms;
    private AuthServerSettings settings;
    private Vertx vertx;
    private Logger logger;

    public AuthProvider() {}

    private AuthProvider(AsyncRealmStore realms, AsyncAccountStore accounts, Vertx vertx) {
        this.realms = realms;
        this.accounts = accounts;
        this.settings = FileConfiguration.instance().getAuthSettings();
        this.logger = new DefaultLogger(vertx, settings.getLogserver());
        this.vertx = vertx;
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

    @Override
    public Logger getLogger() {
        return logger;
    }

    public Vertx getVertx() {
        return vertx;
    }

    public AuthServerSettings getAuthserverSettings() {
        return settings;
    }

    public TokenFactory getClientTokenFactory() {
        return new TokenFactory(settings.getClientSecret());
    }

    public TokenFactory getRealmTokenFactory() {
        return new TokenFactory(settings.getRealmSecret());
    }
}