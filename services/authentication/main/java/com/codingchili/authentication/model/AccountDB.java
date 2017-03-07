package com.codingchili.authentication.model;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.security.*;
import com.codingchili.core.storage.AsyncStorage;
import com.codingchili.core.storage.exception.ValueAlreadyPresentException;
import com.codingchili.core.storage.exception.ValueMissingException;
import io.vertx.core.Handler;

import static io.vertx.core.Future.failedFuture;
import static io.vertx.core.Future.succeededFuture;

/**
 * @author Robin Duda
 *         <p>
 *         Account storage logic.
 */
public class AccountDB implements AsyncAccountStore {
    private final AsyncStorage<AccountMapping> accounts;
    private final HashHelper hasher;

    public AccountDB(AsyncStorage<AccountMapping> map, CoreContext context) {
        this.accounts = map;
        this.hasher = new HashHelper(context);
    }

    @Override
    public void get(Handler<AsyncResult<Account>> future, String username) {
        accounts.get(username, user -> {
            if (user.succeeded()) {
                future.handle(succeededFuture(filter(user.result())));
            } else {
                future.handle(failedFuture(user.cause()));
            }
        });
    }

    @Override
    public void authenticate(Handler<AsyncResult<Account>> future, Account account) {
        accounts.get(account.getUsername(), map -> {
            if (map.succeeded()) {
                authenticate(future, map.result(), account);
            } else {
                fail(future, map);
            }
        });
    }

    @Override
    public void register(Handler<AsyncResult<Account>> future, Account account) {
        Future<String> hashing = Future.future();
        AccountMapping mapping = new AccountMapping(account)
                .setSalt(hasher.salt());

        hashing.setHandler(hash -> {
            mapping.setHash(hash.result());

            accounts.putIfAbsent(mapping, map -> {
                if (map.succeeded()) {
                    future.handle(Future.succeededFuture(filter(account)));
                } else {
                    fail(future, map);
                }
            });
        });
        hasher.hash(hashing, account.getPassword(), mapping.getSalt());
    }

    private void fail(Handler<AsyncResult<Account>> future, AsyncResult result) {
        Throwable cause = result.cause();

        if (cause instanceof ValueAlreadyPresentException) {
            future.handle(failedFuture(new AccountExistsException()));
        } else if (cause instanceof ValueMissingException) {
            future.handle(failedFuture(new AccountMissingException()));
        } else {
            future.handle(failedFuture(cause));
        }
    }

    private void authenticate(Handler<AsyncResult<Account>> future, AccountMapping authenticated, Account unauthenticated) {
        Future<String> hashing = Future.future();

        hashing.setHandler(hash -> {
            if (ByteComparator.compare(hash.result(), authenticated.getHash())) {
                future.handle(succeededFuture(filter(authenticated)));
            } else {
                future.handle(failedFuture(new AccountPasswordException()));
            }
        });
        hasher.hash(hashing, unauthenticated.getPassword(), authenticated.getSalt());
    }

    private Account filter(AccountMapping account) {
        return new Account()
                .setEmail(account.getEmail())
                .setUsername(account.getUsername())
                .setPassword(null);
    }

    private Account filter(Account account) {
        return account.setPassword(null);
    }
}