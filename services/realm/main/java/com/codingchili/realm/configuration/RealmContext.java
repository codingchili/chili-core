package com.codingchili.realm.configuration;

import com.codingchili.realm.instance.context.InstanceSettings;
import com.codingchili.realm.instance.model.entity.PlayerCreature;
import com.codingchili.realm.instance.model.entity.PlayableClass;
import com.codingchili.realm.model.AsyncCharacterStore;
import com.codingchili.realm.model.CharacterDB;
import io.vertx.core.Future;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.codingchili.core.context.*;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.Connection;
import com.codingchili.core.listener.ListenerSettings;
import com.codingchili.core.logging.Level;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.security.Token;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.storage.StorageLoader;

import static com.codingchili.common.Strings.*;
import static com.codingchili.core.logging.Level.ERROR;
import static com.codingchili.realm.configuration.RealmServerSettings.PATH_REALMSERVER;

/**
 * @author Robin Duda
 * <p>
 * Context for realms.
 */
public class RealmContext extends SystemContext implements ServiceContext {
    private Map<String, Connection> connections = new ConcurrentHashMap<>();
    private AsyncCharacterStore characters;
    private RealmSettings settings;
    private Logger logger;

    RealmContext(CoreContext core, RealmSettings settings) {
        super(core);
        this.settings = settings;
        this.logger = core.logger(getClass())
                .setMetadata("realm", realm()::getName);
    }

    public static Future<RealmContext> create(CoreContext core, RealmSettings settings) {
        Future<RealmContext> future = Future.future();

        RealmContext context = new RealmContext(core, settings);

        new StorageLoader<PlayerCreature>(new StorageContext<>(context))
                .withPlugin(context.service().getStorage())
                .withValue(PlayerCreature.class)
                .withCollection(COLLECTION_CHARACTERS)
                .build(storage -> {
                    if (storage.succeeded()) {
                        context.characters = new CharacterDB(storage.result());
                        future.complete(context);
                    } else {
                        future.fail(storage.cause());
                    }
                });

        return future;
    }

    @Override
    public Logger logger(Class aClass) {
        return logger;
    }

    public Map<String, Connection> connections() {
        return connections;
    }

    public RealmSettings realm() {
        return settings;
    }

    public AsyncCharacterStore characters() {
        return characters;
    }

    public RealmServerSettings service() {
        return Configurations.get(PATH_REALMSERVER, RealmServerSettings.class);
    }

    public ListenerSettings getListenerSettings() {
        return new ListenerSettings().setPort(realm().getPort());
    }

    public String address() {
        return realm().getHost();
    }

    public List<InstanceSettings> instances() {
        return realm().getInstances();
    }

    public List<PlayableClass> getClasses() {
        return realm().getClasses();
    }

    public List<PlayableClass> getTemplate() {
        return realm().getClasses();
    }

    public boolean verifyToken(Token token) {
        return new TokenFactory(realm().getTokenBytes()).verifyToken(token);
    }

    public int updateRate() {
        return service().getRealmUpdates();
    }

    public void onRealmStarted(String realm) {
        logger.event(LOG_REALM_START, Level.STARTUP)
                .put(ID_REALM, realm).send();
    }

    public void onRealmRejected(String realm, String message) {
        logger.event(LOG_REALM_REJECTED, Level.WARNING)
                .put(ID_REALM, realm)
                .put(ID_MESSAGE, message).send();
    }

    public void onRealmStopped(Future<Void> future, String realm) {
        logger.event(LOG_REALM_STOP, ERROR)
                .put(ID_REALM, realm).send();

        Delay.forShutdown(future);
    }

    public void onRealmRegistered(String realm) {
        logger.event(LOG_REALM_REGISTERED)
                .put(ID_REALM, realm)
                .send();
    }

    public void onDeployRealmFailure(String realm) {
        logger.event(LOG_REALM_DEPLOY_ERROR, ERROR)
                .put(LOG_MESSAGE, getDeployFailError(realm))
                .send();
    }

    public void onInstanceFailed(String instance, Throwable cause) {
        logger.event(LOG_INSTANCE_DEPLOY_ERROR, ERROR)
                .put(LOG_MESSAGE, getdeployInstanceError(instance, cause))
                .send();
    }
}
