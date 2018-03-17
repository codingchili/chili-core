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
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static com.codingchili.common.Strings.*;
import static com.codingchili.realmregistry.configuration.RealmRegistrySettings.PATH_REALMREGISTRY;

/**
 * @author Robin Duda
 */
public class RegistryContext extends SystemContext implements ServiceContext {
    protected AsyncRealmStore realms;
    protected TokenFactory realmFactory;
    private AsyncRealmStore realmDB;
    private AtomicBoolean loading = new AtomicBoolean(false);
    private Queue<Consumer<AsyncRealmStore>> waiting = new ConcurrentLinkedQueue<>();
    private Logger logger;

    public RegistryContext(CoreContext core) {
        super(core);

        this.realmFactory = new TokenFactory(service().getRealmSecret());
        this.logger = core.logger(getClass());
    }

    public void getRealmStore(Handler<AsyncResult<AsyncRealmStore>> handler) {
        if (!loading.getAndSet(true)) {
            new StorageLoader<RegisteredRealm>().jsonmap(this)
                    .withCollection(COLLECTION_REALMS)
                    .withValue(RegisteredRealm.class)
                    .build(prepare -> {
                        this.realmDB = new RealmDB(prepare.result());
                        waiting.forEach(waiting -> waiting.accept(realmDB));
                        handler.handle(Future.succeededFuture(realmDB));
                    });
        } else {
            waiting.add((store) -> handler.handle(Future.succeededFuture(realmDB)));
        }
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
        logger.event(LOG_REALM_DISCONNECT, Level.ERROR).put(ID_REALM, realm).send();
    }

    public void onRealmUpdated(String realm, int players) {
        logger.event(LOG_REALM_UPDATE, Level.INFO)
                .put(ID_REALM, realm)
                .put(ID_PLAYERS, players).send();
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