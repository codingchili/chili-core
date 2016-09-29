package Authentication.Client;

import Authentication.Configuration.AuthProvider;
import Authentication.Controller.ClientHandler;
import Authentication.Model.Account;
import Authentication.Model.AsyncAccountStore;
import Authentication.ProviderMock;
import Configuration.ConfigMock;
import Protocols.Authorization.Token;
import Protocols.Authorization.TokenFactory;
import Protocols.Serializer;
import Realm.Configuration.RealmSettings;
import Realm.Model.PlayerCharacter;
import Shared.ResponseListener;
import Shared.ResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static Configuration.Strings.*;

/**
 * @author Robin Duda
 *         tests the API from client -> authentication server.
 */

@RunWith(VertxUnitRunner.class)
public class ClientHandlerTest {
    private static final String CHARACTER_NAME = "character";
    private static final String CHARACTER_NAME_DELETED = "character-deleted";
    private static final String PASSWORD = "password";
    private static final String USERNAME = "username";
    private static final String USERNAME_NEW = "new-username";
    private static final String USERNAME_MISSING = "missing-username";
    private static final String PASSWORD_WRONG = "wrong-password";
    private static final String REALM_NAME = "realmName.name";
    private static final String CLASS_NAME = "class.name";
    private static TokenFactory clientToken;
    private static AuthProvider provider;
    private static ClientHandler handler;

    @Rule
    public Timeout timeout = new Timeout(2, TimeUnit.SECONDS);

    @Before
    public void setUp() throws IOException {
        provider = new ProviderMock();
        handler = new ClientHandler(provider);
        RealmSettings realm = new ConfigMock.RealmSettingsMock();
        provider.getRealmStore().put(Future.future(), realm);
        clientToken = new TokenFactory(provider.getAuthserverSettings().getClientSecret());
        addAccount();
    }

    private static void addAccount() {
        AsyncAccountStore accounts = provider.getAccountStore();
        PlayerCharacter add = new PlayerCharacter().setName(CHARACTER_NAME);
        PlayerCharacter delete = new PlayerCharacter().setName(CHARACTER_NAME_DELETED);

        accounts.register(Future.future(), new Account(USERNAME, PASSWORD));
        accounts.upsertCharacter(Future.future(), REALM_NAME, USERNAME, add);
        accounts.upsertCharacter(Future.future(), REALM_NAME, USERNAME, delete);
    }


    @Test
    public void authenticateAccount(TestContext context) {
        Async async = context.async();

        handle(CLIENT_AUTHENTICATE, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        }, account(USERNAME, PASSWORD));
    }

    @Test
    public void failtoAuthenticateAccountWithWrongPassword(TestContext context) {
        Async async = context.async();

        handle(CLIENT_AUTHENTICATE, (response, status) -> {
            context.assertEquals(ResponseStatus.UNAUTHORIZED, status);
            async.complete();
        }, account(USERNAME, PASSWORD_WRONG));
    }

    @Test
    public void failtoAuthenticateAccountWithMissing(TestContext context) {
        Async async = context.async();

        handle(CLIENT_AUTHENTICATE, (response, status) -> {
            context.assertEquals(ResponseStatus.MISSING, status);
            async.complete();
        }, account(USERNAME_MISSING, PASSWORD));
    }

    @Test
    public void registerAccount(TestContext context) {
        Async async = context.async();

        handle(CLIENT_REGISTER, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        }, account(USERNAME_NEW, PASSWORD));
    }

    private JsonObject account(String username, String password) {
        return new JsonObject().put("account", new JsonObject().put("username", username).put("password", password));
    }

    @Test
    public void failRegisterAccountExists(TestContext context) {
        Async async = context.async();

        handle(CLIENT_REGISTER, (response, status) -> {
            context.assertEquals(ResponseStatus.CONFLICT, status);
            async.complete();
        }, account(USERNAME, PASSWORD));
    }

    @Test
    public void retrieveRealmList(TestContext context) {
        Async async = context.async();
        String[] keys = {
                "classes", "description", "name", "resources", "type",
                "secure", "trusted", "proxy", "version"};

        handle(CLIENT_REALM_LIST, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);

            JsonArray list = response.getJsonArray("realms");

            for (int i = 0; i < list.size(); i++) {
                JsonObject realm = list.getJsonObject(i);

                for (String key : keys)
                    context.assertTrue(realm.containsKey(key));
            }

            async.complete();
        });
    }

    @Test
    public void removeCharacter(TestContext context) {
        Async async = context.async();

        handle(CLIENT_CHARACTER_REMOVE, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        }, new JsonObject()
                .put("character", CHARACTER_NAME_DELETED)
                .put("token", getClientToken())
                .put("realmName", REALM_NAME));
    }

    private JsonObject getClientToken() {
        return Serializer.json(new Token(clientToken, USERNAME));
    }

    @Test
    public void failToRemoveMissingCharacter(TestContext context) {
        Async async = context.async();

        handle(CLIENT_CHARACTER_REMOVE, (response, status) -> {
            context.assertEquals(ResponseStatus.ERROR, status);
            async.complete();
        }, new JsonObject()
                .put("character", CHARACTER_NAME + ".MISSING")
                .put("token", getClientToken())
                .put("realmName", REALM_NAME));
    }

    @Test
    public void createCharacter(TestContext context) {
        Async async = context.async();

        handle(CLIENT_CHARACTER_CREATE, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        }, new JsonObject()
                .put("character", CHARACTER_NAME + ".NEW")
                .put("className", CLASS_NAME)
                .put("token", getClientToken())
                .put("realmName", REALM_NAME));
    }

    @Test
    public void failOverwriteExistingCharacter(TestContext context) {
        Async async = context.async();
        handle(CLIENT_CHARACTER_CREATE, (response, status) -> {
            context.assertEquals(ResponseStatus.CONFLICT, status);
            async.complete();
        }, new JsonObject()
                .put("character", CHARACTER_NAME)
                .put("token", getClientToken())
                .put("realmName", REALM_NAME));
    }

    @Test
    public void listCharactersOnRealm(TestContext context) {
        Async async = context.async();

        handle(CLIENT_CHARACTER_LIST, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            context.assertTrue(characterInJsonArray(CHARACTER_NAME, response.getJsonArray("characters")));
            async.complete();
        }, new JsonObject()
                .put("token", getClientToken())
                .put("realmName", REALM_NAME));
    }

    private boolean characterInJsonArray(String charname, JsonArray characters) {
        Boolean found = false;

        for (int i = 0; i < characters.size(); i++) {
            if (characters.getJsonObject(i).getString("name").equals(charname))
                found = true;
        }
        return found;
    }

    @Test
    public void realmDataOnCharacterList(TestContext context) {
        Async async = context.async();

        handle(CLIENT_CHARACTER_LIST, (response, status) -> {
            JsonObject realm = response.getJsonObject("realm");

            context.assertEquals(ResponseStatus.ACCEPTED, status);
            context.assertTrue(realm.containsKey("classes"));
            context.assertTrue(realm.containsKey("name"));
            context.assertTrue(realm.containsKey("afflictions"));
            context.assertTrue(realm.containsKey("template"));

            async.complete();
        }, new JsonObject()
                .put("token", getClientToken())
                .put("realmName", REALM_NAME));
    }

    @Test
    public void realmDataDoesNotIncludeTokenOnCharacterList(TestContext context) {
        Async async = context.async();

        handle(CLIENT_CHARACTER_LIST, (response, status) -> {
            JsonObject realm = response.getJsonObject("realm");

            context.assertEquals(ResponseStatus.ACCEPTED, status);
            context.assertFalse(realm.containsKey("authentication"));
            context.assertFalse(realm.containsKey("token"));

            async.complete();
        }, new JsonObject()
                .put("token", getClientToken())
                .put("realmName", REALM_NAME));
    }

    @Test
    public void createRealmToken(TestContext context) {
        Async async = context.async();

        handle(CLIENT_REALM_TOKEN, (response, status) -> {
            Token token = Serializer.unpack(response, Token.class);

            context.assertEquals(ResponseStatus.ACCEPTED, status);
            context.assertEquals(USERNAME, token.getDomain());

            async.complete();
        }, new JsonObject()
                .put("token", getClientToken())
                .put("realmName", REALM_NAME));
    }

    private void handle(String action, ResponseListener listener) {
        handle(action, listener, new JsonObject().put(ID_TOKEN, getClientToken()));
    }

    private void handle(String action, ResponseListener listener, JsonObject data) {
        try {
            handler.handle(new ClientRequestMock(data, listener, action));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
