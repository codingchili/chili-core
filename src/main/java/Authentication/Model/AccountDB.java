package Authentication.Model;

import Game.Model.PlayerCharacter;
import Utilities.HashHelper;
import Utilities.Serializer;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import java.util.ArrayList;

/**
 * @author Robin Duda
 *         <p>
 *         Implementation of asynchronous account store.
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
    public void addCharacter(Future<Void> future, String realm, String account, PlayerCharacter player) {
        JsonObject query = new JsonObject().put("username", account);
        JsonObject character = new JsonObject().put("$push",
                new JsonObject().put("characters." + realm, Serializer.json(player)));

        client.update(COLLECTION, query, character, result -> {
            if (result.succeeded())
                future.complete();
            else {
                future.fail(result.cause());
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
                JsonArray characters = search.result().getJsonObject("characters").getJsonArray(realm);
                ArrayList<PlayerCharacter> list = new ArrayList<>();

                if (characters != null)
                    for (int i = 0; i < characters.size(); i++)
                        list.add((PlayerCharacter) Serializer.unpack(characters.getJsonObject(i), PlayerCharacter.class));

                future.complete(list);
            } else {
                future.fail(search.cause());
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
