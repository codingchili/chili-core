package com.codingchili.core.Authentication.Model;

import com.codingchili.core.Configuration.Strings;
import com.codingchili.core.Protocols.Util.HashHelper;
import com.codingchili.core.Realm.Instance.Model.PlayerCharacter;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.AsyncMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * @author Robin Duda
 */
public class HazelAccountDB implements AsyncAccountStore {
    private AsyncMap<String, AccountMapping> accounts;
    private HashHelper hasher;


    public static void create(Future<AsyncAccountStore> future, Vertx vertx) {
        vertx.sharedData().<String, AccountMapping>getClusterWideMap(Strings.DB_ACCOUNTS, map -> {
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

    @Override
    public void upsertCharacter(Future future, String realmName, String username, PlayerCharacter character) {
        accounts.get(username, map -> {
            if (map.succeeded() && map.result() != null) {
                AccountMapping mapping = map.result();
                HashMap<String, PlayerCharacter> realm = mapping.getRealms().get(realmName);

                if (realm == null) {
                    mapping.getRealms().put(realmName, new HashMap<>());
                }

                mapping.getRealms().get(realmName).put(character.getName(), character);

                accounts.replace(username, mapping, replace -> {
                    if (replace.succeeded()) {
                        future.complete();
                    } else {
                        future.fail(replace.cause());
                    }
                });

            } else {
                future.fail(map.cause());
            }
        });
    }

    @Override
    public void findCharacters(Future<ArrayList<PlayerCharacter>> future, String realm, String username) {
        accounts.get(username, map -> {
            if (map.succeeded()) {
                ArrayList<PlayerCharacter> result = new ArrayList<>();
                HashMap<String, PlayerCharacter> characters = map.result().getRealms().get(realm);

                if (characters != null) {
                    result.addAll(characters.keySet().stream().map(characters::get).collect(Collectors.toList()));
                }

                future.complete(result);
            } else {
                future.fail(map.cause());
            }
        });
    }

    @Override
    public void findCharacter(Future<PlayerCharacter> future, String realm, String username, String character) {
        accounts.get(username, map -> {
            if (map.succeeded()) {

                if (map.result() != null) {
                    AccountMapping mapping = map.result();
                    HashMap<String, HashMap<String, PlayerCharacter>> realms = mapping.getRealms();

                    if (realms.containsKey(realm) && realms.get(realm).containsKey(character)) {
                        future.complete(realms.get(realm).get(character));
                    } else {
                        future.fail(new CharacterMissingException());
                    }
                } else {
                    future.fail(new AccountMissingException());
                }
            } else {
                future.fail(map.cause());
            }
        });
    }

    @Override
    public void removeCharacter(Future future, String realm, String username, String character) {
        accounts.get(username, map -> {
            if (map.succeeded()) {

                if (map.result() != null) {
                    AccountMapping mapping = map.result();
                    HashMap<String, PlayerCharacter> players = mapping.getRealms().get(realm);

                    if (players != null) {
                        PlayerCharacter removed = mapping.getRealms().get(realm).remove(character);

                        if (removed == null) {
                            future.fail(new CharacterMissingException());
                        } else {
                            accounts.replace(username, mapping, remove -> {
                                if (remove.succeeded()) {
                                    future.complete();
                                } else {
                                    future.fail(remove.cause());
                                }
                            });
                        }
                    } else {
                        future.fail(new RealmMissingException());
                    }
                } else {
                    future.fail(new AccountMissingException());
                }
            } else {
                future.fail(map.cause());
            }
        });
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
