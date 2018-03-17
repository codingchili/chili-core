package com.codingchili.authentication.configuration;

import com.codingchili.authentication.model.AccountDB;
import com.codingchili.authentication.model.AccountMapping;
import com.codingchili.authentication.model.AsyncAccountStore;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.ServiceContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.security.Account;
import com.codingchili.core.security.Token;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.storage.StorageLoader;
import io.vertx.core.Future;

import static com.codingchili.authentication.configuration.AuthenticationSettings.PATH_AUTHSERVER;
import static com.codingchili.common.Strings.*;
import static com.codingchili.core.logging.Level.WARNING;

/**
 * @author Robin Duda
 * <p>
 * Authentication service context.
 */
public class AuthenticationContext extends SystemContext implements ServiceContext {
    private AsyncAccountStore accounts;
    private Logger logger;

    public AuthenticationContext(CoreContext core) {
        super(core);
        this.logger = core.logger(getClass());
    }

    public static void create(Future<AuthenticationContext> future, CoreContext core) {
        AuthenticationContext context = new AuthenticationContext(core);
        new StorageLoader<AccountMapping>(context)
                .withPlugin(context.service().getStorage())
                .withCollection(COLLECTION_ACCOUNTS)
                .withValue(AccountMapping.class)
                .build(prepare -> {
                    context.accounts = new AccountDB(prepare.result(), context);
                    future.complete(context);
                });
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

    public AuthenticationSettings service() {
        return Configurations.get(PATH_AUTHSERVER, AuthenticationSettings.class);
    }

    public void onAuthenticationFailure(Account account, String host) {
        logger.event(LOG_ACCOUNT_UNAUTHORIZED, WARNING)
                .put(LOG_USER, account.getUsername())
                .put(LOG_REMOTE, host).send();
    }

    public void onAuthenticated(String username, String host) {
        logger.event(LOG_ACCOUNT_AUTHENTICATED)
                .put(LOG_USER, username)
                .put(LOG_REMOTE, host).send();
    }

    public void onRegistered(String username, String host) {
        logger.event(LOG_ACCOUNT_REGISTERED)
                .put(LOG_USER, username)
                .put(LOG_REMOTE, host).send();
    }
}