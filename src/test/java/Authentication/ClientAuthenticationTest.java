package Authentication;

import Authentication.ClientRequestMock.ResponseStatus;
import Authentication.Model.AuthorizationHandler.Access;
import Authentication.Controller.ClientHandler;
import Authentication.Controller.ClientProtocol;
import Authentication.Model.Provider;
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
public class ClientAuthenticationTest {
    private static final String CHARACTER_NAME = "character";
    private static final String CHARACTER_NAME_DELETED = "character-deleted";
    private static final String PASSWORD = "password";
    private static final String USERNAME = "username";
    private static final String USERNAME_NEW = "new-username";
    private static final String USERNAME_MISSING = "missing-username";
    private static final String PASSWORD_WRONG = "wrong-password";
    private static final String REALM_NAME = "realm.name";
    private static final String CLASS_NAME = "class.name";
    private static final byte[] REALM_SECRET = "realm.secret".getBytes();
    private static ClientProtocol protocol;
    private static TokenFactory factory = new TokenFactory("client.secret".getBytes());
    private static Vertx vertx;

    @Rule
    public Timeout timeout = new Timeout(2, TimeUnit.SECONDS);

    @Before
    public void setUp() throws IOException {
        vertx = Vertx.vertx();

        Provider provider = new ProviderMock(vertx);
        protocol = provider.clientProtocol(Access.PUBLIC);

        // todo the client will receive another instance of the protocol on which it will .use on.
        // todo the store implementation must be fixed and the vertx/deploy responsobility sorted!

        new ClientHandler(provider);
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void authenticateAccount(TestContext context) {
        handle(ClientProtocol.AUTHENTICATE, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
        }, account(USERNAME, PASSWORD));
    }

    @Test
    public void failtoAuthenticateAccountWithWrongPassword(TestContext context) {
        handle(ClientProtocol.AUTHENTICATE, (response, status) -> {
            context.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        }, account(USERNAME, PASSWORD_WRONG));
    }

    private void handle(String action, ResponseListener listener, JsonObject data) {
        protocol.handle(action, new ClientRequestMock(data, listener));
    }

    @Test
    public void failtoAuthenticateAccountWithMissing(TestContext context) {
        handle(ClientProtocol.AUTHENTICATE, (response, status) -> {
            context.assertEquals(ResponseStatus.MISSING, status);
        }, account(USERNAME_MISSING, PASSWORD));
    }

    @Test
    public void registerAccount(TestContext context) {
        handle(ClientProtocol.REGISTER, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
        }, account(USERNAME_NEW, PASSWORD));
    }

    private JsonObject account(String username, String password) {
        return new JsonObject().put("account", new JsonObject().put("username", username).put("password", password));
    }

    @Test
    public void failRegisterAccountExists(TestContext context) {
        handle(ClientProtocol.REGISTER, (response, status) -> {
            context.assertEquals(ResponseStatus.CONFLICT, status);
        }, account(USERNAME, PASSWORD));
    }

    @Test
    public void retrieveRealmList(TestContext context) {
        String[] keys = {
                "classes", "description", "name", "remote", "resources", "type",
                "secure", "trusted", "port", "version"};

        handle(ClientProtocol.REALMLIST, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);

            JsonArray list = response.getJsonArray("realms");

            for (int i = 0; i < list.size(); i++) {
                JsonObject realm = list.getJsonObject(i);

                for (String key : keys)
                    context.assertTrue(realm.containsKey(key));
            }
        }, account(USERNAME_MISSING, PASSWORD));
    }

    @Test
    public void removeCharacter(TestContext context) {
        handle(ClientProtocol.CHARACTERREMOVE, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
        }, new JsonObject()
                .put("character", CHARACTER_NAME_DELETED)
                .put("token", getClientToken())
                .put("realm", REALM_NAME));
    }

    private JsonObject getClientToken() {
        return Serializer.json(new Token(factory, USERNAME));
    }

    @Test
    public void failToRemoveCharacterWhenInvalidToken(TestContext context) {
        handle(ClientProtocol.CHARACTERREMOVE, (response, status) -> {
            context.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        }, new JsonObject()
                .put("character", CHARACTER_NAME_DELETED)
                .put("token", getInvalidClientToken())
                .put("realm", REALM_NAME));
    }

    @Test
    public void failToRemoveMissingCharacter(TestContext context) {
        handle(ClientProtocol.CHARACTERREMOVE, (response, status) -> {
            context.assertEquals(ResponseStatus.ERROR, status);
        }, new JsonObject()
                .put("character", CHARACTER_NAME + ".MISSING")
                .put("token", getClientToken())
                .put("realm", REALM_NAME));
    }

    private JsonObject getInvalidClientToken() {
        return Serializer.json(new Token(new TokenFactory("invalid".getBytes()), USERNAME));
    }

    @Test
    public void createCharacter(TestContext context) {
        handle(ClientProtocol.CHARACTERCREATE, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
        }, new JsonObject()
                .put("character", CHARACTER_NAME + ".NEW")
                .put("className", CLASS_NAME)
                .put("token", getClientToken())
                .put("realm", REALM_NAME));
    }

    @Test
    public void failToCreateCharacterWhenInvalidToken(TestContext context) {
        handle(ClientProtocol.CHARACTERCREATE, (response, status) -> {
            context.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        }, new JsonObject()
                .put("character", CHARACTER_NAME)
                .put("token", getInvalidClientToken())
                .put("realm", REALM_NAME));
    }

    @Test
    public void failOverwriteExistingCharacter(TestContext context) {
        handle(ClientProtocol.CHARACTERCREATE, (response, status) -> {
            context.assertEquals(ResponseStatus.CONFLICT, status);
        }, new JsonObject()
                .put("character", CHARACTER_NAME)
                .put("token", getClientToken())
                .put("realm", REALM_NAME));
    }

    @Test
    public void listCharactersOnRealm(TestContext context) {
        handle(ClientProtocol.CHARACTERLIST, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            context.assertTrue(characterInJsonArray(CHARACTER_NAME, response.getJsonArray("characters")));
        }, new JsonObject()
                .put("token", getClientToken())
                .put("realm", REALM_NAME));
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
    public void failListCharactersOnRealmWhenInvalidToken(TestContext context) {
        handle(ClientProtocol.CHARACTERLIST, (response, status) -> {
            context.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        }, new JsonObject()
                .put("token", Serializer.json(getInvalidClientToken()))
                .put("realm", REALM_NAME));
    }

    @Test
    public void realmDataOnCharacterList(TestContext context) {
        handle(ClientProtocol.CHARACTERLIST, (response, status) -> {
            JsonObject realm = response.getJsonObject("realm");

            context.assertEquals(ResponseStatus.ACCEPTED, status);
            context.assertTrue(realm.containsKey("classes"));
            context.assertTrue(realm.containsKey("name"));
            context.assertTrue(realm.containsKey("afflictions"));
            context.assertTrue(realm.containsKey("template"));

        }, new JsonObject()
                .put("token", getClientToken())
                .put("realm", REALM_NAME));
    }

    @Test
    public void realmDataDoesNotIncludeTokenOnCharacterList(TestContext context) {
        handle(ClientProtocol.CHARACTERLIST, (response, status) -> {
            JsonObject realm = response.getJsonObject("realm");

            context.assertEquals(ResponseStatus.ACCEPTED, status);
            context.assertFalse(realm.containsKey("authentication"));
            context.assertFalse(realm.containsKey("token"));

        }, new JsonObject()
                .put("token", getClientToken())
                .put("realm", REALM_NAME));
    }

    @Test
    public void createRealmToken(TestContext context) {
        handle(ClientProtocol.REALMTOKEN, (response, status) -> {
            Token token = (Token) Serializer.unpack(response, Token.class);
            TokenFactory factory = new TokenFactory(REALM_SECRET);

            context.assertEquals(ResponseStatus.ACCEPTED, status);
            context.assertEquals(USERNAME, token.getDomain());
            context.assertTrue(factory.verifyToken(token));

        }, new JsonObject()
                .put("token", getClientToken())
                .put("realm", REALM_NAME));
    }

    @Test
    public void failCreateRealmTokenWhenInvalidToken(TestContext context) {
        handle(ClientProtocol.REALMTOKEN, (response, status) -> {
            context.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        }, new JsonObject()
                .put("token", getInvalidClientToken())
                .put("realm", REALM_NAME));
    }
}
