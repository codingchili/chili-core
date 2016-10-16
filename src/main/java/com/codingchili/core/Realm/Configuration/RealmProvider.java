package com.codingchili.core.Realm.Configuration;

import com.codingchili.core.Configuration.Provider;
import com.codingchili.core.Logging.Model.DefaultLogger;
import com.codingchili.core.Logging.Model.Logger;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 */
public class RealmProvider implements Provider {
    private final RealmServerSettings server;
    private final RealmSettings realm;
    private final Logger logger;
    private final Vertx vertx;

    public RealmProvider(Vertx vertx, RealmServerSettings server, RealmSettings realm) {
        this.server = server;
        this.realm = realm;
        this.vertx = vertx;
        this.logger = new DefaultLogger(vertx, server.getLogserver());
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
}
