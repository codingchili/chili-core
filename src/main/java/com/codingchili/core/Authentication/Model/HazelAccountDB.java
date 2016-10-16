package com.codingchili.core.Authentication.Model;

import com.codingchili.core.Configuration.Strings;
import com.codingchili.core.Protocols.Util.HashHelper;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.AsyncMap;

/**
 * @author Robin Duda
 */
public class HazelAccountDB implements AsyncAccountStore {
    private final AsyncMap<String, AccountMapping> accounts;
    private final HashHelper hasher;


    public static void create(Future<AsyncAccountStore> future, Vertx vertx) {
        vertx.sharedData().<String, AccountMapping>getClusterWideMap(Strings.MAP_ACCOUNTS, map -> {
            if (map.succeeded()) {
                future.complete(new HazelAccountDB(map.result(), vertx));
            } else {
                future.fail(map.cause());
            }
        });
    }

    HazelAccountDB(AsyncMap<String, AccountMapping> map, Vertx vertx) {
        this.accounts = map;
        this.hasher = new HashHelper(vertx);
    }


    @Override
    public void find(Future<Account> future, String username) {
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
                if (map.result() != null) {
                    authenticate(future, map.result(), account);
                } else {
                    future.fail(new AccountMissingException());
                }
            } else {
                future.fail(map.cause());
            }
        });
    }

    @Override
    public void register(Future<Account> future, Account account) {
        Future<String> hashing = Future.future();
        String salt = HashHelper.generateSalt();
        AccountMapping mapping = new AccountMapping(account);

        hashing.setHandler(hash -> {
            mapping.setSalt(salt);
            mapping.setHash(hash.result());

            accounts.putIfAbsent(account.getUsername(), mapping, map -> {
                if (map.succeeded()) {

                    if (map.result() == null) {
                        future.complete(filter(account));
                    } else {
                        future.fail(new AccountExistsException());
                    }
                } else {
                    future.fail(map.cause());
                }
            });
        });

        hasher.hash(hashing, account.getPassword(), salt);
    }


    private void authenticate(Future<Account> future, AccountMapping authenticated, Account unauthenticated) {
        Future<String> hashing = Future.future();

        hashing.setHandler(hash -> {
            boolean verified = HashHelper.compare(hash.result(), authenticated.getHash());

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
