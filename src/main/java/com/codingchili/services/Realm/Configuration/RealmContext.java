package com.codingchili.services.realm.configuration;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.*;

import com.codingchili.core.context.*;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.Level;
import com.codingchili.core.security.Token;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.storage.*;

import com.codingchili.services.realm.instance.configuration.InstanceSettings;
import com.codingchili.services.realm.instance.model.PlayerCharacter;
import com.codingchili.services.realm.instance.model.PlayerClass;
import com.codingchili.services.realm.model.AsyncCharacterStore;
import com.codingchili.services.realm.model.CharacterDB;

import static com.codingchili.services.realm.configuration.RealmServerSettings.PATH_REALMSERVER;
import static com.codingchili.services.Shared.Strings.*;

/**
 * @author Robin Duda
 */
public class RealmContext extends ServiceContext {
    private AsyncCharacterStore characters;
    private String settings;

    public RealmContext(Vertx vertx) {
        super(vertx);
    }

    private RealmContext(AsyncCharacterStore characters, RealmSettings realm, Vertx vertx) {
        super(vertx);

        this.settings = realm.getPath();
        this.characters = characters;
    }

    public static void create(Future<RealmContext> future, RealmSettings realm, Vertx vertx) {
        Future<AsyncStorage<String, PlayerCharacter>> create = Future.future();

        create.setHandler(map -> {
            future.complete(new RealmContext(new CharacterDB(map.result()), realm, vertx));
        });

        StorageLoader.prepare()
                .withClass(PlayerCharacter.class)
                .withPlugin(PrivateMap.class)
                .withCollection(COLLECTION_CHARACTERS)
                .withContext(new StorageContext<>(vertx))
                .build(create);
    }

    public ArrayList<EnabledRealm> getEnabled() {
        return service().getEnabled();
    }

    public AsyncCharacterStore getCharacterStore() {
        return characters;
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

    public ArrayList<InstanceSettings> instances() {
        return realm().getInstances();
    }

    public ArrayList<PlayerClass> getClasses() {
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
        log(event(LOG_REALM_START, Level.STARTUP)
                .put(ID_REALM, realm));
    }

    public void onRealmRejected(String realm) {
        log(event(LOG_REALM_REJECTED, Level.WARNING)
                .put(ID_REALM, realm));
    }

    public void onRealmStopped(Future<Void> future, String realm) {
        log(event(LOG_REALM_STOP, Level.SEVERE)
                .put(ID_REALM, realm));

        Delay.forShutdown(future);
    }

    public void onRealmRegistered(String realm) {
        log(event(LOG_REALM_REGISTERED)
                .put(ID_REALM, realm));
    }

    public void onDeployRealmFailure(String realm) {
        log(event(LOG_REALM_DEPLOY_ERROR, Level.SEVERE)
                .put(LOG_MESSAGE, getDeployFailError(realm)));
    }
}
