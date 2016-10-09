package com.codingchili.core.Realm.Instance.Configuration;

import com.codingchili.core.Configuration.FileConfiguration;
import com.codingchili.core.Configuration.Provider;
import com.codingchili.core.Logging.Model.DefaultLogger;
import com.codingchili.core.Logging.Model.Logger;
import com.codingchili.core.Protocols.Util.TokenFactory;
import com.codingchili.core.Realm.Configuration.RealmServerSettings;
import com.codingchili.core.Realm.Configuration.RealmSettings;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 */
public class InstanceProvider implements Provider {
    private RealmServerSettings server;
    private RealmSettings realm;
    private InstanceSettings instance;
    private Vertx vertx;

    public InstanceProvider(RealmSettings realm, InstanceSettings instance, Vertx vertx) {
        this.server = FileConfiguration.instance().getRealmServerSettings();
        this.vertx = vertx;
        this.realm = realm;
        this.instance = instance;
    }

    @Override
    public Logger getLogger() {
        return new DefaultLogger(vertx, server.getLogserver());
    }

    public String getAddress() {
        return instance.getName() + "." + realm.getRemote();
    }

    public TokenFactory getTokenFactory() {
        return new TokenFactory(realm.getAuthentication());
    }

    public RealmSettings getRealm() {
        return realm;
    }

    public InstanceSettings getInstance() {
        return instance;
    }
}
