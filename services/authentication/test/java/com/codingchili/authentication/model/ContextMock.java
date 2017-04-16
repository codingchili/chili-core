package com.codingchili.authentication.model;

import com.codingchili.authentication.configuration.AuthenticationContext;
import com.codingchili.authentication.configuration.AuthenticationSettings;
import io.vertx.core.Vertx;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.storage.PrivateMap;


/**
 * @author Robin Duda
 */
public class ContextMock extends AuthenticationContext {
    private AuthenticationSettings settings = new AuthenticationSettings();
    private AsyncAccountStore accounts;

    public ContextMock(Vertx vertx) {
        super(new SystemContext(vertx));
        settings.setClientSecret("client-secret".getBytes());

        accounts = new AccountDB(new PrivateMap<>(new StorageContext<>(this)), this);
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