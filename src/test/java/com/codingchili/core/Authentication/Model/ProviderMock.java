package com.codingchili.core.Authentication.Model;

import com.codingchili.core.Authentication.Configuration.AuthProvider;
import com.codingchili.core.Authentication.Configuration.AuthServerSettings;
import com.codingchili.core.Logging.LoggerMock;
import com.codingchili.core.Logging.Model.Logger;
import com.codingchili.core.Protocols.Util.TokenFactory;
import com.codingchili.core.Shared.AsyncMapMock;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 */
public class ProviderMock extends AuthProvider {
    private AuthServerSettings settings = new AuthServerSettings();
    private AsyncRealmStore realms;
    private AsyncAccountStore accounts;

    public ProviderMock() {
        super();

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
    public Logger getLogger() {
        return new LoggerMock();
    }

    @Override
    public AuthServerSettings getAuthserverSettings() {
        return settings;
    }

    @Override
    public TokenFactory getClientTokenFactory() {
        return new TokenFactory(settings.getClientSecret());
    }

    @Override
    public TokenFactory getRealmTokenFactory() {
        return new TokenFactory(settings.getRealmSecret());
    }
}