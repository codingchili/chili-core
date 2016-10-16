package com.codingchili.core.Realm.Configuration;

import com.codingchili.core.Configuration.Provider;
import com.codingchili.core.Logging.Model.DefaultLogger;
import com.codingchili.core.Logging.Model.Logger;
import com.codingchili.core.Realm.Model.AsyncCharacterStore;
import com.codingchili.core.Realm.Model.HazelCharacterDB;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 */
public class RealmProvider implements Provider {
    private AsyncCharacterStore characters;
    private RealmServerSettings server;
    private RealmSettings realm;
    private Logger logger;
    private Vertx vertx;

    public RealmProvider() {}

    private RealmProvider(AsyncCharacterStore characters, RealmServerSettings server, RealmSettings realm, Vertx vertx) {
        this.server = server;
        this.realm = realm;
        this.vertx = vertx;
        this.characters = characters;
        this.logger = new DefaultLogger(vertx, server.getLogserver());
    }

    public static void create(Future<RealmProvider> future, RealmServerSettings server, RealmSettings realm, Vertx vertx) {
        Future<AsyncCharacterStore> create = Future.future();

        create.setHandler(map -> {
            future.complete(new RealmProvider(create.result(), server, realm, vertx));
        });

        HazelCharacterDB.create(create, vertx, realm.getName());
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    public Vertx getVertx() {
        return vertx;
    }

    public RealmServerSettings getServer() {
        return server;
    }

    public RealmSettings getRealm() {
        return realm;
    }

    public AsyncCharacterStore getCharacterStore() {
        return characters;
    }
}
