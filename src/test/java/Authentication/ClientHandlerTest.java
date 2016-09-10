package Authentication;

import Authentication.Configuration.AuthProvider;
import Authentication.Controller.ClientHandler;
import Authentication.Controller.ClientRequest;
import Authentication.Model.Account;
import Authentication.Model.AsyncAccountStore;
import Authentication.Model.RealmStore;
import Configuration.ConfigMock;
import Configuration.Strings;
import Protocols.Authorization.Token;
import Protocols.Authorization.TokenFactory;
import Protocols.AuthorizationHandler.Access;
import Protocols.Exception.AuthorizationRequiredException;
import Protocols.Exception.HandlerMissingException;
import Protocols.PacketHandler;
import Protocols.Protocol;
import Protocols.Serializer;
import Realm.Model.PlayerCharacter;
import Shared.ResponseListener;
import Shared.ResponseStatus;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

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
    private static Protocol<PacketHandler<ClientRequest>> protocol;
    private static AuthProvider provider;
    private static Vertx vertx;

    @Rule
    public Timeout timeout = new Timeout(20, TimeUnit.SECONDS);

    @BeforeClass
    public static void setUp(TestContext context) throws IOException {
        Async async = context.async();

        Vertx.clusteredVertx(new VertxOptions(), result -> {
            RealmStore realms = new RealmStore(vertx);
            realms.put(new ConfigMock.RealmSettingsMock());
            provider = new ProviderMock(vertx);
            clientToken = new TokenFactory(provider.getAuthserverSettings().getClientSecret());
            protocol = provider.clientProtocol();
            new ClientHandler(provider);
            addAccount(context);

            async.complete();
        });
    }

    private static void addAccount(TestContext context) {
        Async async = context.async();
        AsyncAccountStore accounts = provider.getAccountStore();
        Account account = new Account(USERNAME, PASSWORD);
        Future<Account> future = Future.future();

        future.setHandler(result -> {
            PlayerCharacter add = new PlayerCharacter().setName(CHARACTER_NAME);
            PlayerCharacter delete = new PlayerCharacter().setName(CHARACTER_NAME_DELETED);
            Future<Void> addFuture = Future.future();
            Future<Void> deleteFuture = Future.future();

            CompositeFuture.all(addFuture, deleteFuture).setHandler(complete -> {
                async.complete();
            });

            accounts.upsertCharacter(addFuture, REALM_NAME, USERNAME, add);
            accounts.upsertCharacter(deleteFuture, REALM_NAME, USERNAME, delete);
        });

        accounts.register(future, account);
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void authenticateAccount(TestContext context) {
        Async async = context.async();

        handle(Strings.CLIENT_AUTHENTICATE, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        }, account(USERNAME, PASSWORD));
    }

    @Test
    public void failtoAuthenticateAccountWithWrongPassword(TestContext context) {
        Async async = context.async();

        handle(Strings.CLIENT_AUTHENTICATE, (response, status) -> {
            context.assertEquals(ResponseStatus.UNAUTHORIZED, status);
            async.complete();
        }, account(USERNAME, PASSWORD_WRONG));
    }

    private void handle(String action, ResponseListener listener) {
        handle(action, listener, null);
    }

    private void handle(String action, ResponseListener listener, JsonObject data) {
        try {
            protocol.get(action, Access.AUTHORIZE).handle(new ClientRequestMock(data, listener));
        } catch (AuthorizationRequiredException | HandlerMissingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Test
    public void failtoAuthenticateAccountWithMissing(TestContext context) {
        Async async = context.async();

        handle(Strings.CLIENT_AUTHENTICATE, (response, status) -> {
            context.assertEquals(ResponseStatus.MISSING, status);
            async.complete();
        }, account(USERNAME_MISSING, PASSWORD));
    }

    @Test
    public void registerAccount(TestContext context) {
        Async async = context.async();

        handle(Strings.CLIENT_REGISTER, (response, status) -> {
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

        handle(Strings.CLIENT_REGISTER, (response, status) -> {
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

        handle(Strings.CLIENT_REALM_LIST, (response, status) -> {
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

        handle(Strings.CLIENT_CHARACTER_REMOVE, (response, status) -> {
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

        handle(Strings.CLIENT_CHARACTER_REMOVE, (response, status) -> {
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

        handle(Strings.CLIENT_CHARACTER_CREATE, (response, status) -> {
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
        handle(Strings.CLIENT_CHARACTER_CREATE, (response, status) -> {
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

        handle(Strings.CLIENT_CHARACTER_LIST, (response, status) -> {
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

        handle(Strings.CLIENT_CHARACTER_LIST, (response, status) -> {
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

        handle(Strings.CLIENT_CHARACTER_LIST, (response, status) -> {
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

        handle(Strings.CLIENT_REALM_TOKEN, (response, status) -> {
            Token token = (Token) Serializer.unpack(response, Token.class);

            context.assertEquals(ResponseStatus.ACCEPTED, status);
            context.assertEquals(USERNAME, token.getDomain());

            async.complete();
        }, new JsonObject()
                .put("token", getClientToken())
                .put("realmName", REALM_NAME));
    }
}
