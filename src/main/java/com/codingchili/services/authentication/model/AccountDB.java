package com.codingchili.services.authentication.model;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

import com.codingchili.core.security.ByteComparator;
import com.codingchili.core.security.HashHelper;
import com.codingchili.core.storage.AsyncStorage;
import com.codingchili.core.storage.exception.ValueAlreadyPresentException;
import com.codingchili.core.storage.exception.ValueMissingException;

/**
 * @author Robin Duda
 */
public class AccountDB implements AsyncAccountStore {
    private final AsyncStorage<String, AccountMapping> accounts;
    private final HashHelper hasher;


    public AccountDB(AsyncStorage<String, AccountMapping> map, Vertx vertx) {
        this.accounts = map;
        this.hasher = new HashHelper(vertx);
    }

    @Override
    public void get(Future<Account> future, String username) {
        accounts.get(username, user -> {
            if (user.succeeded()) {
                future.complete(filter(user.result()));
            } else {
                future.fail(user.cause());
            }
        });
    }

    @Override
    public void authenticate(Future<Account> future, Account account) {
        accounts.get(account.getUsername(), map -> {
            if (map.succeeded()) {
                authenticate(future, map.result(), account);
            } else {
                try {
                    throw map.cause();
                } catch (ValueMissingException e) {
                    future.fail(new AccountMissingException());
                } catch (Throwable throwable) {
                    future.fail(throwable);
                }
            }
        });
    }

    @Override
    public void register(Future<Account> future, Account account) {
        Future<String> hashing = Future.future();
        String salt = HashHelper.salt();
        AccountMapping mapping = new AccountMapping(account);

        hashing.setHandler(hash -> {
            mapping.setSalt(salt);
            mapping.setHash(hash.result());

            accounts.putIfAbsent(account.getUsername(), mapping, map -> {
                if (map.succeeded()) {
                    future.complete(filter(account));
                } else {
                    try {
                        throw map.cause();
                    } catch (ValueAlreadyPresentException e) {
                        future.fail(new AccountExistsException());
                    } catch (Throwable throwable) {
                        future.fail(map.cause());
                    }
                }
            });
        });

        hasher.hash(hashing, account.getPassword(), salt);
    }

    private void authenticate(Future<Account> future, AccountMapping authenticated, Account unauthenticated) {
        Future<String> hashing = Future.future();

        hashing.setHandler(hash -> {
            boolean verified = ByteComparator.compare(hash.result(), authenticated.getHash());

            if (verified) {
                future.complete(filter(authenticated));
            } else {
                future.fail(new AccountPasswordException());
            }
        });
        hasher.hash(hashing, unauthenticated.getPassword(), authenticated.getSalt());
    }

    private Account filter(AccountMapping account) {
        return new Account(account).setPassword(null);
    }

    private Account filter(Account account) {
        return account.setPassword(null);
    }
}
