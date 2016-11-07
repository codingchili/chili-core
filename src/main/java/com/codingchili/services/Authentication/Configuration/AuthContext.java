package com.codingchili.services.Authentication.Configuration;

import io.vertx.core.*;

import com.codingchili.core.Context.ServiceContext;
import com.codingchili.core.Files.Configurations;
import com.codingchili.core.Logging.Level;
import com.codingchili.core.Security.Token;
import com.codingchili.core.Security.TokenFactory;

import com.codingchili.services.Authentication.Model.*;

import static com.codingchili.services.Authentication.Configuration.AuthServerSettings.PATH_AUTHSERVER;
import static com.codingchili.services.Shared.Strings.*;

/**
 * @author Robin Duda
 */
public class AuthContext extends ServiceContext {
    private AsyncAccountStore accounts;
    private AsyncRealmStore realms;

    public AuthContext(Vertx vertx) {
        super(vertx);
    }

    private AuthContext(AsyncRealmStore realms, AsyncAccountStore accounts, Vertx vertx) {
        super(vertx);

        this.realms = realms;
        this.accounts = accounts;
    }

    public static void create(Future<AuthContext> future, Vertx vertx) {
        Future<AsyncRealmStore> realmFuture = Future.future();
        Future<AsyncAccountStore> accountFuture = Future.future();

        CompositeFuture.all(realmFuture, accountFuture).setHandler(initialization -> {
            AsyncRealmStore realms = (AsyncRealmStore) initialization.result().list().get(0);
            AsyncAccountStore accounts = (AsyncAccountStore) initialization.result().list().get(1);

            future.complete(new AuthContext(realms, accounts, vertx));
        });

        HazelAccountDB.create(accountFuture, vertx);
        HazelRealmDB.create(realmFuture, vertx);
    }

    public AsyncRealmStore getRealmStore() {
        return realms;
    }

    public AsyncAccountStore getAccountStore() {
        return accounts;
    }

    public boolean verifyClientToken(Token token) {
        return new TokenFactory(service().getClientSecret()).verifyToken(token);
    }

    public Token signClientToken(String domain) {
        return new Token(new TokenFactory(service().getClientSecret()), domain);
    }

    public boolean verifyRealmToken(Token token) {
        return new TokenFactory(service().getRealmSecret()).verifyToken(token);
    }

    protected AuthServerSettings service() {
        return Configurations.get(PATH_AUTHSERVER, AuthServerSettings.class);
    }

    public Boolean isTrustedRealm(String name) {
        return service().isTrustedRealm(name);
    }

    public int realmTimeout() {
        return service().getRealmTimeout();
    }

    public void onAuthenticationFailure(String username, String host) {
        log(event(LOG_ACCOUNT_UNAUTHORIZED, Level.WARNING)
                .put(LOG_USER, username)
                .put(LOG_REMOTE, host));
    }

    public void onAuthenticated(String username, String host) {
        log(event(LOG_ACCOUNT_AUTHENTICATED)
                .put(LOG_USER, username)
                .put(LOG_REMOTE, host));
    }

    public void onRegistered(String username, String host) {
        log(event(LOG_ACCOUNT_REGISTERED)
                .put(LOG_USER, username)
                .put(LOG_REMOTE, host));
    }

    public void onRealmDisconnect(String realm) {
        log(event(LOG_REALM_DISCONNECT, Level.SEVERE)
                .put(ID_REALM, realm));
    }

    public void onRealmUpdated(String realm, int players) {
        log(event(LOG_REALM_UPDATE, Level.INFO)
                .put(ID_REALM, realm)
                .put(ID_PLAYERS, players));
    }

    public void onStaleClearError(Throwable cause) {
        logger.onError(cause);
    }

    public void onStaleRemoveError(Throwable cause) {
        logger.onError(cause);
    }
}