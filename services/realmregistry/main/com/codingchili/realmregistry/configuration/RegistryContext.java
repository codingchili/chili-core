package com.codingchili.realmregistry.configuration;

import com.codingchili.realmregistry.model.AsyncRealmStore;
import com.codingchili.realmregistry.model.RealmDB;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.context.ServiceContext;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.Level;
import com.codingchili.core.security.Token;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.storage.*;

import static com.codingchili.common.Strings.*;
import static com.codingchili.realmregistry.configuration.RealmRegistrySettings.PATH_REALMREGISTRY;

/**
 * @author Robin Duda
 */
public class RegistryContext extends ServiceContext {
    protected AsyncRealmStore realms;
    protected TokenFactory realmFactory;

    protected RegistryContext(Vertx vertx) {
        super(vertx);
    }

    private RegistryContext(AsyncRealmStore realms, Vertx vertx) {
        super(vertx);

        this.realmFactory = new TokenFactory(service().getRealmSecret());
        this.realms = realms;
    }

    public static void create(Future<RegistryContext> future, Vertx vertx) {
        RegistryContext context = new RegistryContext(vertx);
        Future<AsyncStorage<String, RealmSettings>> realmFuture = Future.future();

        realmFuture.setHandler(complete -> {
            AsyncRealmStore realms = new RealmDB(realmFuture.result());
            future.complete(new RegistryContext(realms, vertx));
        });

        StorageLoader.prepare()
                .withContext(context)
                .withCollection(COLLECTION_REALMS)
                .withClass(JsonObject.class)
                .withPlugin(MongoDBMap.class)
                .build(realmFuture);
    }

    public AsyncRealmStore getRealmStore() {
        return realms;
    }

    public boolean verifyRealmToken(Token token) {
        return new TokenFactory(service().getRealmSecret()).verifyToken(token);
    }

    public boolean verifyClientToken(Token token) {
        return new TokenFactory(service().getClientSecret()).verifyToken(token);
    }

    protected RealmRegistrySettings service() {
        return Configurations.get(PATH_REALMREGISTRY, RealmRegistrySettings.class);
    }

    public Boolean isTrustedRealm(String name) {
        return service().isTrustedRealm(name);
    }

    public int realmTimeout() {
        return service().getRealmTimeout();
    }

    public void onRealmDisconnect(String realm) {
        log(event(LOG_REALM_DISCONNECT, Level.SEVERE)
                .put(ID_REALM, realm));
    }

    public void onRealmUpdated(String realm, int players) {
        log(event(LOG_REALM_UPDATE, Level.INFO)
                .put(ID_REALM, realm)
                .put(ID_PLAYERS, players));
    }

    public void onStaleClearError(Throwable cause) {
        logger.onError(cause);
    }

    public void onStaleRemoveError(Throwable cause) {
        logger.onError(cause);
    }

    public TokenFactory getRealmFactory() {
        return realmFactory;
    }
}