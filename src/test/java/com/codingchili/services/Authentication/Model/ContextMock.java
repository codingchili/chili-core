package com.codingchili.services.authentication.model;

import io.vertx.core.Vertx;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.storage.PrivateMap;

import com.codingchili.services.authentication.configuration.AuthContext;
import com.codingchili.services.authentication.configuration.AuthServerSettings;


/**
 * @author Robin Duda
 */
public class ContextMock extends AuthContext {
    private AuthServerSettings settings = new AuthServerSettings();
    private AsyncRealmStore realms;
    private AsyncAccountStore accounts;

    public ContextMock(Vertx vertx) {
        super(vertx);
        settings.setRealmSecret("realm-secret".getBytes());
        settings.setClientSecret("client-secret".getBytes());

        realms = new RealmDB(new PrivateMap<>(new StorageContext(this)));
        accounts = new AsyncAccountDB(new PrivateMap<>(new StorageContext(this)), Vertx.vertx());
    }

    @Override
    public AsyncRealmStore getRealmStore() {
        return realms;
    }

    @Override
    public AsyncAccountStore getAccountStore() {
        return accounts;
    }

    @Override
    public AuthServerSettings service() {
        return settings;
    }


    public TokenFactory getClientFactory() {
        return new TokenFactory(settings.getClientSecret());
    }


    public TokenFactory getRealmFactory() {
        return new TokenFactory(settings.getRealmSecret());
    }
}