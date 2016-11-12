package com.codingchili.services.Realm.Configuration;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.ArrayList;

import com.codingchili.core.Context.Delay;
import com.codingchili.core.Context.ServiceContext;
import com.codingchili.core.Files.Configurations;
import com.codingchili.core.Logging.Level;
import com.codingchili.core.Security.Token;
import com.codingchili.core.Security.TokenFactory;

import com.codingchili.services.Realm.Instance.Configuration.InstanceSettings;
import com.codingchili.services.Realm.Instance.Model.PlayerCharacter;
import com.codingchili.services.Realm.Instance.Model.PlayerClass;
import com.codingchili.services.Realm.Model.AsyncCharacterStore;
import com.codingchili.services.Realm.Model.HazelCharacterDB;

import static com.codingchili.services.Realm.Configuration.RealmServerSettings.PATH_REALMSERVER;
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
        Future<AsyncCharacterStore> create = Future.future();

        create.setHandler(map -> {
            future.complete(new RealmContext(create.result(), realm, vertx));
        });

        HazelCharacterDB.create(create, vertx, realm.getName());
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
