package com.codingchili.realmregistry.configuration;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.ServiceContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.Level;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.security.Token;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.storage.StorageLoader;
import com.codingchili.realmregistry.model.AsyncRealmStore;
import com.codingchili.realmregistry.model.RealmDB;
import io.vertx.core.Future;

import static com.codingchili.common.Strings.*;
import static com.codingchili.realmregistry.configuration.RealmRegistrySettings.PATH_REALMREGISTRY;

/**
 * @author Robin Duda
 */
public class RegistryContext extends SystemContext implements ServiceContext {
    protected AsyncRealmStore realms;
    protected TokenFactory realmFactory;
    private Logger logger;

    protected RegistryContext(AsyncRealmStore realms, CoreContext core) {
        super(core);

        this.realmFactory = new TokenFactory(service().getRealmSecret());
        this.realms = realms;
        this.logger = core.logger(getClass());
    }

    public static void create(Future<RegistryContext> future, CoreContext core) {
        new StorageLoader<RegisteredRealm>().diskIndex(core)
                .withCollection(COLLECTION_REALMS)
                .withClass(RegisteredRealm.class)
                .build(prepare -> {
                    AsyncRealmStore realms = new RealmDB(prepare.result());
                    future.complete(new RegistryContext(realms, core));
                });
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

    public RealmRegistrySettings service() {
        return Configurations.get(PATH_REALMREGISTRY, RealmRegistrySettings.class);
    }

    public Boolean isTrustedRealm(String name) {
        return service().isTrustedRealm(name);
    }

    public int realmTimeout() {
        return service().getRealmTimeout();
    }

    public void onRealmDisconnect(String realm) {
        logger.log(event(LOG_REALM_DISCONNECT, Level.SEVERE)
                .put(ID_REALM, realm));
    }

    public void onRealmUpdated(String realm, int players) {
        logger.log(event(LOG_REALM_UPDATE, Level.INFO)
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