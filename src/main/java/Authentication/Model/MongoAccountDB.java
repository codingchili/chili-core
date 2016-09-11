package Authentication.Model;

import Authentication.Configuration.DatabaseSettings;
import Configuration.Strings;
import Realm.Model.PlayerCharacter;
import Protocols.Authorization.HashHelper;
import Protocols.Serializer;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import java.util.ArrayList;

/**
 * @author Robin Duda
 *         <p>
 *         Implementation of asynchronous account store using MongoDb.
 */
public class MongoAccountDB implements AsyncAccountStore {
    private static final String COLLECTION = Strings.DB_COLLECTION;
    private MongoClient client;

    public MongoAccountDB(Vertx vertx, DatabaseSettings settings) {
        this.client = MongoClient.createShared(vertx, settings.toJson());
    }


    @Override
    public void find(Future<Account> future, String username) {
        JsonObject query = new JsonObject().put(Strings.DB_USER, username);

        client.findOne(COLLECTION, query, null, find -> {
            if (find.succeeded() && find.result() != null)
                future.complete(filter((AccountMapping) Serializer.unpack(find.result(), AccountMapping.class)));
            else
                future.fail(new AccountMissingException());
        });
    }

    @Override
    public void register(Future<Account> future, Account registrant) {
        JsonObject query = new JsonObject().put(Strings.DB_USER, registrant.getUsername());
        JsonObject account = new JsonObject(Serializer.pack(new AccountMapping(registrant)));

        client.findOne(COLLECTION, query, null, search -> {

            if (search.succeeded() && search.result() == null) {
                String salt = HashHelper.generateSalt();
                account.put(Strings.DB_SALT, salt);
                account.put(Strings.DB_HASH, HashHelper.hash(registrant.getPassword(), salt));

                client.save(COLLECTION, account, save -> {
                    if (save.succeeded()) {
                        future.complete(filter(registrant));
                    } else
                        future.fail(save.cause());
                });
            } else {
                future.fail(new AccountExistsException());
            }

        });
    }

    @Override
    public void upsertCharacter(Future future, String realm, String username, PlayerCharacter player) {
        JsonObject query = new JsonObject().put(Strings.DB_USER, username);
        JsonObject character = new JsonObject().put("$set",
                new JsonObject().put(Strings.DB_CHARACTERS + "." + realm + "." + player.getName(), Serializer.json(player)));

        client.updateCollection(COLLECTION, query, character, update -> {
            if (update.succeeded())
                future.complete();
            else {
                future.fail(update.cause());
            }
        });
    }

    @Override
    public void removeCharacter(Future future, String realm, String username, String character) {
        Future<ArrayList<PlayerCharacter>> find = Future.future();

        find.setHandler(found -> {
            JsonObject query = new JsonObject()
                    .put(Strings.DB_USER, username);
            JsonObject field = new JsonObject()
                    .put("$unset",
                            new JsonObject().put(Strings.DB_CHARACTERS + "." + realm + "." + character, ""));

            client.updateCollection(COLLECTION, query, field, remove -> {

                if (remove.result().getDocModified() != 0)
                    future.complete();
                else
                    future.fail(remove.cause());
            });

        });
        findCharacters(find, realm, username);
    }

    @Override
    public void findCharacter(Future<PlayerCharacter> future, String realmName, String username, String character) {
        JsonObject query = new JsonObject()
                .put(Strings.DB_USER, username);
        JsonObject fields = new JsonObject()
                .put(Strings.DB_CHARACTERS + "." + realmName + "." + character, 1)
                .put("_id", 0);

        client.findOne(COLLECTION, query, fields, search -> {
            if (search.succeeded() && search.result() != null) {
                JsonObject characters = search.result().getJsonObject(Strings.DB_CHARACTERS);
                JsonObject realm = characters.getJsonObject(realmName);

                if (realm != null && realm.containsKey(character)) {
                    future.complete((PlayerCharacter) Serializer.unpack(realm.getJsonObject(character), PlayerCharacter.class));
                } else {
                    future.fail(new CharacterMissingException());
                }
            } else {
                future.fail(new CharacterMissingException());
            }
        });
    }

    @Override
    public void findCharacters(Future<ArrayList<PlayerCharacter>> future, String realm, String username) {
        JsonObject query = new JsonObject()
                .put(Strings.DB_USER, username);
        JsonObject fields = new JsonObject()
                .put(Strings.DB_CHARACTERS + "." + realm, 1);

        client.findOne(COLLECTION, query, fields, search -> {
            if (search.succeeded() && search.result() != null) {
                JsonObject characters = search.result().getJsonObject(Strings.DB_CHARACTERS).getJsonObject(realm);
                ArrayList<PlayerCharacter> result = new ArrayList<>();

                if (characters != null)
                    for (String key : characters.fieldNames())
                        result.add((PlayerCharacter) Serializer.unpack(characters.getJsonObject(key), PlayerCharacter.class));

                future.complete(result);
            } else {
                future.fail(new AccountMissingException());
            }
        });
    }

    @Override
    public void authenticate(Future<Account> future, Account unauthenticated) {
        JsonObject query = new JsonObject().put(Strings.DB_USER, unauthenticated.getUsername());

        client.findOne(COLLECTION, query, null, find -> {
            if (find.succeeded() && find.result() != null) {

                AccountMapping account = (AccountMapping) Serializer.unpack(find.result(), AccountMapping.class);

                if (authenticate(account, unauthenticated)) {
                    future.complete(filter(account));
                } else
                    future.fail(new AccountPasswordException());
            } else {
                future.fail(new AccountMissingException());
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
