package Authentication.Model;

import Configuration.Strings;
import Protocols.Authorization.HashHelper;
import Realm.Model.PlayerCharacter;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.AsyncMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * @author Robin Duda
 */
public class
HazelAccountDB implements AsyncAccountStore {
    private AsyncMap<String, AccountMapping> accounts;


    public static void create(Future<AsyncAccountStore> realmFuture, Vertx vertx) {
        vertx.sharedData().<String, AccountMapping>getClusterWideMap(Strings.DB_COLLECTION, map -> {
            if (map.succeeded()) {
                realmFuture.complete(new HazelAccountDB(map.result()));
            } else {
                realmFuture.fail(map.cause());
            }
        });
    }

    private HazelAccountDB(AsyncMap<String, AccountMapping> map) {
        this.accounts = map;
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
                    if (authenticate(map.result(), account)) {
                        future.complete(filter(account));
                    } else {
                        future.fail(new AccountPasswordException());
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
    public void register(Future<Account> future, Account account) {
        String salt = HashHelper.generateSalt();
        AccountMapping mapping = new AccountMapping(account);

        mapping.setSalt(salt);
        mapping.setHash(HashHelper.hash(account.getPassword(), salt));

        accounts.putIfAbsent(account.getUsername(), mapping, map -> {
            if (map.succeeded()) {

                // Result is null when no previous value was in the map.
                if (map.result() == null) {
                    future.complete(filter(account));
                } else {
                    future.fail(new AccountExistsException());
                }

            } else {
                future.fail(map.cause());
            }
        });
    }

    @Override
    public void upsertCharacter(Future future, String realmName, String username, PlayerCharacter character) {
        accounts.get(username, map -> {
            if (map.succeeded()) {
                AccountMapping mapping = map.result();
                HashMap<String, PlayerCharacter> realm = mapping.getRealms().get(realmName);

                // Add a realm to an account if not existing.
                if (realm == null) {
                    realm = new HashMap<>();
                    mapping.getRealms().put(realmName, realm);
                }

                realm.put(character.getName(), character);

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
                    future.complete(mapping.getRealms().get(realm).get(character));
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
                    mapping.getRealms().get(realm).remove(character);

                    accounts.replace(username, mapping, remove -> {
                        if (remove.succeeded()) {
                            future.complete();
                        } else {
                            future.fail(remove.cause());
                        }
                    });

                } else {
                    future.fail(new AccountMissingException());
                }
            } else {
                future.fail(map.cause());
            }
        });
    }

    private boolean authenticate(AccountMapping authenticated, Account unauthenticated) {
        String hash = HashHelper.hash(unauthenticated.getPassword(), authenticated.getSalt());
        return HashHelper.compare(hash, authenticated.getHash());
    }

    private Account filter(AccountMapping account) {
        return new Account(account).setPassword(null);
    }

    private Account filter(Account account) {
        return account.setPassword(null);
    }
}
