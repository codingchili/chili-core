package Authentication.Model;

import Game.Model.PlayerCharacter;
import Protocols.Authorization.HashHelper;
import Protocols.Serializer;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import java.util.ArrayList;

/**
 * @author Robin Duda
 *         <p>
 *         Implementation of asynchronous account store using MongoDb.
 */
public class AccountDB implements AsyncAccountStore {
    private static final String COLLECTION = "accounts";
    private MongoClient client;

    public AccountDB(MongoClient client) {
        this.client = client;
    }


    @Override
    public void find(Future<Account> future, String username) {
        JsonObject query = new JsonObject().put("username", username);

        client.findOne(COLLECTION, query, null, account -> {
            if (account.succeeded() && account.result() != null)
                future.complete(filter((AccountMapping) Serializer.unpack(account.result(), AccountMapping.class)));
            else
                future.fail(new AccountMissingException());
        });
    }

    @Override
    public void register(Future<Account> future, Account registrant) {
        JsonObject query = new JsonObject().put("username", registrant.getUsername());
        JsonObject account = new JsonObject(Serializer.pack(new AccountMapping(registrant)));

        client.findOne(COLLECTION, query, null, search -> {

            if (search.succeeded() && search.result() == null) {
                String salt = HashHelper.generateSalt();
                account.put("salt", salt);
                account.put("hash", HashHelper.hash(registrant.getPassword(), salt));

                client.save(COLLECTION, account, result -> {
                    if (result.succeeded()) {
                        registrant.setPassword(null);
                        future.complete(registrant);
                    } else
                        future.fail(result.cause());
                });
            } else {
                future.fail(new AccountExistsException());
            }

        });
    }

    @Override
    public void addCharacter(Future future, String realm, String username, PlayerCharacter player) {
        JsonObject query = new JsonObject().put("username", username);
        JsonObject character = new JsonObject().put("$set",
                new JsonObject().put("characters." + realm + "." + player.getName(), Serializer.json(player)));

        client.update(COLLECTION, query, character, result -> {
            if (result.succeeded())
                future.complete();
            else {
                future.fail(result.cause());
            }
        });
    }

    @Override
    public void removeCharacter(Future future, String realm, String username, String character) {
        Future<ArrayList<PlayerCharacter>> find = Future.future();

        find.setHandler(found -> {
            JsonObject query = new JsonObject()
                    .put("username", username);
            JsonObject field = new JsonObject()
                    .put("$unset",
                            new JsonObject().put("characters." + realm + "." + character, ""));

            client.update(COLLECTION, query, field, remove -> {

                if (remove.succeeded())
                    future.complete();
                else
                    future.fail(remove.cause());
            });

        });
        findCharacters(find, realm, username);
    }

    @Override
    public void findCharacter(Future<PlayerCharacter> future, String realmName, String username, String characterName) {
        JsonObject query = new JsonObject()
                .put("username", username);
        JsonObject fields = new JsonObject()
                .put("characters." + realmName + "." + characterName, 1)
                .put("_id", 0);

        client.findOne(COLLECTION, query, fields, search -> {
            if (search.succeeded() && search.result() != null) {
                JsonObject characters = search.result().getJsonObject("characters");
                JsonObject realm = characters.getJsonObject(realmName);

                if (realm != null && realm.containsKey(characterName)) {
                    future.complete((PlayerCharacter) Serializer.unpack(realm.getJsonObject(characterName), PlayerCharacter.class));
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
                .put("username", username);
        JsonObject fields = new JsonObject()
                .put("characters." + realm, 1);

        client.findOne(COLLECTION, query, fields, search -> {
            if (search.succeeded() && search.result() != null) {
                JsonObject characters = search.result().getJsonObject("characters").getJsonObject(realm);
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
        JsonObject query = new JsonObject().put("username", unauthenticated.getUsername());

        client.findOne(COLLECTION, query, null, result -> {
            if (result.succeeded() && result.result() != null) {

                AccountMapping account = (AccountMapping) Serializer.unpack(result.result(), AccountMapping.class);

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
        return new Account(account);
    }
}
