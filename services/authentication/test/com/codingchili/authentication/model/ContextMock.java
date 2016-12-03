package com.codingchili.authentication.model;

import com.codingchili.authentication.configuration.AuthenticationContext;
import io.vertx.core.Vertx;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.storage.PrivateMap;

import com.codingchili.authentication.configuration.AuthenticationSettings;


/**
 * @author Robin Duda
 */
public class ContextMock extends AuthenticationContext {
    private AuthenticationSettings settings = new AuthenticationSettings();
    private AsyncAccountStore accounts;

    public ContextMock(Vertx vertx) {
        super(vertx);
        settings.setClientSecret("client-secret".getBytes());

        accounts = new AccountDB(new PrivateMap<>(new StorageContext<>(this)), vertx);
    }

    @Override
    public AsyncAccountStore getAccountStore() {
        return accounts;
    }

    @Override
    public AuthenticationSettings service() {
        return settings;
    }
}