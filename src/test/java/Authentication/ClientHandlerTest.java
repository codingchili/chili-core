package Authentication;

import Authentication.ClientRequestMock.ResponseStatus;
import Authentication.Controller.ClientHandler;
import Authentication.Controller.ClientRequest;
import Authentication.Controller.PacketHandler;
import Authentication.Controller.Protocol;
import Authentication.Model.AuthorizationHandler.Access;
import Authentication.Model.AuthorizationRequiredException;
import Authentication.Model.HandlerMissingException;
import Authentication.Model.Provider;
import Authentication.Model.RealmStore;
import Utilities.Serializer;
import Utilities.Token;
import Utilities.TokenFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
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
    private static TokenFactory realmToken;
    private static Protocol<PacketHandler<ClientRequest>> protocol;
    private static Vertx vertx;

    @Rule
    public Timeout timeout = new Timeout(2, TimeUnit.SECONDS);

    @Before
    public void setUp() throws IOException {
        vertx = Vertx.vertx();
        RealmStore realms = new RealmStore(vertx);
        realms.put(new ConfigMock.RealmSettingsMock());
        Provider provider = new ProviderMock(vertx);
        clientToken = new TokenFactory(provider.getAuthserverSettings().getClientSecret());
        realmToken = new TokenFactory(new ConfigMock.RealmSettingsMock().getAuthentication().getToken().getKey().getBytes());
        protocol = provider.clientProtocol();
        new ClientHandler(provider);
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void authenticateAccount(TestContext context) {
        handle(Protocol.AUTHENTICATE, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
        }, account(USERNAME, PASSWORD));
    }

    @Test
    public void failtoAuthenticateAccountWithWrongPassword(TestContext context) {
        handle(Protocol.AUTHENTICATE, (response, status) -> {
            context.assertEquals(ResponseStatus.UNAUTHORIZED, status);
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
        handle(Protocol.AUTHENTICATE, (response, status) -> {
            context.assertEquals(ResponseStatus.MISSING, status);
        }, account(USERNAME_MISSING, PASSWORD));
    }

    @Test
    public void registerAccount(TestContext context) {
        handle(Protocol.REGISTER, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
        }, account(USERNAME_NEW, PASSWORD));
    }

    private JsonObject account(String username, String password) {
        return new JsonObject().put("account", new JsonObject().put("username", username).put("password", password));
    }

    @Test
    public void failRegisterAccountExists(TestContext context) {
        handle(Protocol.REGISTER, (response, status) -> {
            context.assertEquals(ResponseStatus.CONFLICT, status);
        }, account(USERNAME, PASSWORD));
    }

    @Test
    public void retrieveRealmList(TestContext context) {
        String[] keys = {
                "classes", "description", "name", "resources", "type",
                "secure", "trusted", "port", "version"};

        handle(Protocol.REALMLIST, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);

            JsonArray list = response.getJsonArray("realms");

            for (int i = 0; i < list.size(); i++) {
                JsonObject realm = list.getJsonObject(i);

                for (String key : keys)
                    context.assertTrue(realm.containsKey(key));
            }
        });
    }

    @Test
    public void removeCharacter(TestContext context) {
        handle(Protocol.CHARACTERREMOVE, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
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
        handle(Protocol.CHARACTERREMOVE, (response, status) -> {
            context.assertEquals(ResponseStatus.ERROR, status);
        }, new JsonObject()
                .put("character", CHARACTER_NAME + ".MISSING")
                .put("token", getClientToken())
                .put("realmName", REALM_NAME));
    }

    @Test
    public void createCharacter(TestContext context) {
        handle(Protocol.CHARACTERCREATE, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
        }, new JsonObject()
                .put("character", CHARACTER_NAME + ".NEW")
                .put("className", CLASS_NAME)
                .put("token", getClientToken())
                .put("realmName", REALM_NAME));
    }

    @Test
    public void failOverwriteExistingCharacter(TestContext context) {
        handle(Protocol.CHARACTERCREATE, (response, status) -> {
            context.assertEquals(ResponseStatus.CONFLICT, status);
        }, new JsonObject()
                .put("character", CHARACTER_NAME)
                .put("token", getClientToken())
                .put("realmName", REALM_NAME));
    }

    @Test
    public void listCharactersOnRealm(TestContext context) {
        handle(Protocol.CHARACTERLIST, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            context.assertTrue(characterInJsonArray(CHARACTER_NAME, response.getJsonArray("characters")));
        }, new JsonObject()
                .put("token", getClientToken())
                .put("realmName", REALM_NAME));
    }

    private boolean characterInJsonArray(String username, JsonArray characters) {
        Boolean found = false;

        for (int i = 0; i < characters.size(); i++) {
            if (characters.getJsonObject(i).getString("name").equals(username))
                found = true;
        }
        return found;
    }

    @Test
    public void realmDataOnCharacterList(TestContext context) {
        handle(Protocol.CHARACTERLIST, (response, status) -> {
            JsonObject realm = response.getJsonObject("realm");

            context.assertEquals(ResponseStatus.ACCEPTED, status);
            context.assertTrue(realm.containsKey("classes"));
            context.assertTrue(realm.containsKey("name"));
            context.assertTrue(realm.containsKey("afflictions"));
            context.assertTrue(realm.containsKey("template"));

        }, new JsonObject()
                .put("token", getClientToken())
                .put("realmName", REALM_NAME));
    }

    @Test
    public void realmDataDoesNotIncludeTokenOnCharacterList(TestContext context) {
        handle(Protocol.CHARACTERLIST, (response, status) -> {
            JsonObject realm = response.getJsonObject("realm");

            context.assertEquals(ResponseStatus.ACCEPTED, status);
            context.assertFalse(realm.containsKey("authentication"));
            context.assertFalse(realm.containsKey("token"));

        }, new JsonObject()
                .put("token", getClientToken())
                .put("realmName", REALM_NAME));
    }

    @Test
    public void createRealmToken(TestContext context) {
        handle(Protocol.REALMTOKEN, (response, status) -> {
            Token token = (Token) Serializer.unpack(response, Token.class);

            context.assertEquals(ResponseStatus.ACCEPTED, status);
            context.assertEquals(USERNAME, token.getDomain());
            context.assertTrue(realmToken.verifyToken(token));

        }, new JsonObject()
                .put("token", getClientToken())
                .put("realmName", REALM_NAME));
    }
}
