package com.codingchili.services.Authentication.Model;

import io.vertx.core.Vertx;

import com.codingchili.core.Security.TokenFactory;
import com.codingchili.core.Testing.AsyncMapMock;

import com.codingchili.services.Authentication.Configuration.AuthContext;
import com.codingchili.services.Authentication.Configuration.AuthServerSettings;


/**
 * @author Robin Duda
 */
public class ContextMock extends AuthContext {
    private AuthServerSettings settings = new AuthServerSettings();
    private AsyncRealmStore realms;
    private AsyncAccountStore accounts;

    public ContextMock() {
        super(Vertx.vertx());
        settings.setRealmSecret("realm-secret".getBytes());
        settings.setClientSecret("client-secret".getBytes());

        realms = new HazelRealmDB(new AsyncMapMock<>());
        accounts = new HazelAccountDB(new AsyncMapMock<>(), Vertx.vertx());
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