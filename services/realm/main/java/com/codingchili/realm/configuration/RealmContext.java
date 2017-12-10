package com.codingchili.realm.configuration;

import com.codingchili.core.context.*;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.Level;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.security.Token;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.storage.StorageLoader;
import com.codingchili.realm.instance.context.InstanceSettings;
import com.codingchili.realm.instance.model.entity.PlayerCharacter;
import com.codingchili.realm.instance.model.entity.PlayerClass;
import com.codingchili.realm.model.AsyncCharacterStore;
import com.codingchili.realm.model.CharacterDB;
import io.vertx.core.Future;

import java.util.List;

import static com.codingchili.common.Strings.*;
import static com.codingchili.core.logging.Level.ERROR;
import static com.codingchili.realm.configuration.RealmServerSettings.PATH_REALMSERVER;

/**
 * @author Robin Duda
 * <p>
 * Context for realms.
 */
public class RealmContext extends SystemContext implements ServiceContext {
    private Logger logger;
    private String settings;

    public RealmContext(CoreContext core) {
        super(core);
        this.logger = core.logger(getClass());
    }

    public Future<AsyncCharacterStore> getCharacterStore(RealmSettings realm) {
        Future<AsyncCharacterStore> future = Future.future();

        new StorageLoader<PlayerCharacter>(new StorageContext<>(this))
                .withPlugin(service().getStorage())
                .withValue(PlayerCharacter.class)
                .withCollection(COLLECTION_CHARACTERS)
                .build(storage -> {
                    if (storage.succeeded()) {
                        settings = realm.getPath();
                        future.complete(new CharacterDB(storage.result()));
                    } else {
                        future.fail(storage.cause());
                    }
                });

        return future;
    }

    public RealmSettings realm() {
        return Configurations.get(settings, RealmSettings.class);
    }

    public RealmServerSettings service() {
        return Configurations.get(PATH_REALMSERVER, RealmServerSettings.class);
    }

    public String address() {
        return realm().getRemote();
    }

    public List<InstanceSettings> instances() {
        return realm().getInstances();
    }

    public List<PlayerClass> getClasses() {
        return realm().getClasses();
    }

    public PlayerCharacter getTemplate() {
        return realm().getTemplate();
    }

    public boolean verifyToken(Token token) {
        return new TokenFactory(realm().getTokenBytes()).verifyToken(token);
    }

    public int updateRate() {
        return service().getRealmUpdates();
    }

    public void onRealmStarted(String realm) {
        event(LOG_REALM_START, Level.STARTUP)
                .put(ID_REALM, realm).send();
    }

    public void onRealmRejected(String realm, String message) {
        event(LOG_REALM_REJECTED, Level.WARNING)
                .put(ID_REALM, realm)
                .put(ID_MESSAGE, message).send();
    }

    public void onRealmStopped(Future<Void> future, String realm) {
        event(LOG_REALM_STOP, ERROR)
                .put(ID_REALM, realm).send();

        Delay.forShutdown(future);
    }

    public void onRealmRegistered(String realm) {
        event(LOG_REALM_REGISTERED).put(ID_REALM, realm).send();
    }

    public void onDeployRealmFailure(String realm) {
        event(LOG_REALM_DEPLOY_ERROR, ERROR).put(LOG_MESSAGE, getDeployFailError(realm)).send();
    }

    public void onInstanceFailed(String instance, Throwable cause) {
        event(LOG_INSTANCE_DEPLOY_ERROR, ERROR).put(LOG_MESSAGE, getdeployInstanceError(instance, cause)).send();
    }
}
