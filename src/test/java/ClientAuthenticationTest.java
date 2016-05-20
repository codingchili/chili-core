import Authentication.Model.Account;
import Authentication.Model.AsyncAccountStore;
import Configuration.AuthServerSettings;
import Configuration.Config;
import Configuration.RealmSettings;
import Game.Model.PlayerCharacter;
import Mock.AccountStoreMock;
import Utilities.Serializer;
import Utilities.Token;
import Utilities.TokenFactory;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.HashMap;

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
    private static TokenFactory factory;
    private static AuthServerSettings authconfig;
    private static RealmSettings realmconfig;
    private static Vertx vertx;

    @BeforeClass
    public static void setUp(TestContext context) throws IOException {
        Async async = context.async();
        AsyncAccountStore accounts = new AccountStoreMock();
        vertx = Vertx.vertx();
        authconfig = Config.instance().getAuthSettings();
        realmconfig = Config.instance().getGameServerSettings().getRealms().get(0);

        factory = new TokenFactory(authconfig.getClientSecret());
        addAccount(accounts);

        vertx.deployVerticle(new Authentication.Server(accounts));

        RealmAuthenticationTest.registerRealm(authconfig, realmconfig, Future.future().setHandler(done -> {
            async.complete();
        }));
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    private static void addAccount(AsyncAccountStore accounts) {
        Account account = new Account().setPassword(PASSWORD).setUsername(USERNAME);
        account.getCharacters().put(realmconfig.getName(), new HashMap<>());

        account.getCharacters().get(realmconfig.getName()).put(CHARACTER_NAME,
                new PlayerCharacter().setName(CHARACTER_NAME));

        account.getCharacters().get(realmconfig.getName()).put(CHARACTER_NAME_DELETED,
                new PlayerCharacter().setName(CHARACTER_NAME_DELETED));

        accounts.register(Future.future(), account);
    }

    @Test
    public void authenticateAccount(TestContext context) {
        Async async = context.async();
        Account account = new Account()
                .setUsername(USERNAME)
                .setPassword(PASSWORD);

        postRequest(response -> {
            context.assertEquals(response.statusCode(), HttpResponseStatus.OK.code());
            async.complete();
        }, "/api/authenticate").end(Serializer.pack(account));
    }

    private HttpClientRequest postRequest(Handler<HttpClientResponse> handler, String resource) {
        return vertx.createHttpClient().post(authconfig.getClientPort(), "localhost", resource, handler);
    }

    @Test
    public void failtoAuthenticateAccountWithWrongPassword(TestContext context) {
        Async async = context.async();
        Account account = new Account()
                .setUsername(USERNAME)
                .setPassword(PASSWORD_WRONG);

        postRequest(response -> {
            context.assertEquals(response.statusCode(), HttpResponseStatus.UNAUTHORIZED.code());
            async.complete();
        }, "/api/authenticate").end(Serializer.pack(account));
    }

    @Test
    public void failtoAuthenticateAccountWithMissing(TestContext context) {
        Async async = context.async();
        Account account = new Account()
                .setUsername(USERNAME_MISSING)
                .setPassword(PASSWORD);

        postRequest(response -> {
            context.assertEquals(response.statusCode(), HttpResponseStatus.NOT_FOUND.code());
            async.complete();
        }, "/api/authenticate").end(Serializer.pack(account));
    }

    @Test
    public void registerAccount(TestContext context) {
        Async async = context.async();
        Account account = new Account()
                .setUsername(USERNAME_NEW)
                .setPassword(PASSWORD);

        postRequest(response -> {
            context.assertEquals(response.statusCode(), HttpResponseStatus.OK.code());
            async.complete();
        }, "/api/register").end(Serializer.pack(account));
    }

    @Test
    public void failRegisterAccountExists(TestContext context) {
        Async async = context.async();
        Account account = new Account()
                .setUsername(USERNAME)
                .setPassword(PASSWORD);

        postRequest(response -> {
            context.assertEquals(response.statusCode(), HttpResponseStatus.CONFLICT.code());
            async.complete();
        }, "/api/register").end(Serializer.pack(account));
    }

    @Test
    public void retrieveRealmList(TestContext context) {
        Async async = context.async();
        String[] keys = {
                "classes", "description", "name", "remote", "resources", "type",
                "secure", "trusted", "port", "version"};

        getRequest(response -> {
            context.assertEquals(response.statusCode(), HttpResponseStatus.OK.code());

            response.bodyHandler(data -> {
                JsonArray list = data.toJsonArray();

                for (int i = 0; i < list.size(); i++) {
                    JsonObject realm = list.getJsonObject(i);

                    for (String key : keys)
                        context.assertTrue(realm.containsKey(key));
                }
                async.complete();
            });

        }, "/api/realmlist");
    }

    private HttpClient getRequest(Handler<HttpClientResponse> handler, String resource) {
        return vertx.createHttpClient().getNow(authconfig.getClientPort(), "localhost", resource, handler);
    }

    @Test
    public void removeCharacter(TestContext context) {
        Async async = context.async();

        postRequest(response -> {
            context.assertEquals(HttpResponseStatus.OK.code(), response.statusCode());
            async.complete();
        }, "/api/character-remove").end(new JsonObject()
                .put("name", CHARACTER_NAME_DELETED)
                .put("token", Serializer.json(getClientToken()))
                .put("realm", realmconfig.getName()).encode());
    }

    private Token getClientToken() {
        return new Token(factory, USERNAME);
    }

    @Test
    public void failToRemoveCharacterWhenInvalidToken(TestContext context) {
        Async async = context.async();

        postRequest(response -> {
            context.assertEquals(HttpResponseStatus.UNAUTHORIZED.code(), response.statusCode());
            async.complete();
        }, "/api/character-remove").end(new JsonObject()
                .put("name", CHARACTER_NAME)
                .put("token", Serializer.json(getInvalidClientToken()))
                .put("realm", realmconfig.getName()).encode());
    }

    @Test
    public void failToRemoveMissingCharacter(TestContext context) {
        Async async = context.async();

        postRequest(response -> {
            context.assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), response.statusCode());
            async.complete();
        }, "/api/character-remove").end(new JsonObject()
                .put("name", CHARACTER_NAME + ".MISSING")
                .put("token", Serializer.json(getClientToken()))
                .put("realm", realmconfig.getName()).encode());
    }

    private Token getInvalidClientToken() {
        return new Token(new TokenFactory("invalid".getBytes()), USERNAME);
    }

    @Test
    public void createCharacter(TestContext context) {
        Async async = context.async();

        postRequest(response -> {
            context.assertEquals(HttpResponseStatus.OK.code(), response.statusCode());
            async.complete();
        }, "/api/character-create").end(new JsonObject()
                .put("name", CHARACTER_NAME + ".NEW")
                .put("className", realmconfig.getClasses().get(0).getName())
                .put("token", Serializer.json(getClientToken()))
                .put("realm", realmconfig.getName()).encode());
    }

    @Test
    public void failToCreateCharacterWhenInvalidToken(TestContext context) {
        Async async = context.async();

        postRequest(response -> {
            context.assertEquals(HttpResponseStatus.UNAUTHORIZED.code(), response.statusCode());
            async.complete();
        }, "/api/character-create").end(new JsonObject()
                .put("token", Serializer.json(getInvalidClientToken())).encode());
    }

    @Test
    public void failOverwriteExistingCharacter(TestContext context) {
        Async async = context.async();

        postRequest(response -> {
            context.assertEquals(HttpResponseStatus.CONFLICT.code(), response.statusCode());
            async.complete();
        }, "/api/character-create").end(new JsonObject()
                .put("name", CHARACTER_NAME)
                .put("token", Serializer.json(getClientToken()))
                .put("realm", realmconfig.getName()).encode());
    }

    @Test
    public void listCharactersOnRealm(TestContext context) {
        Async async = context.async();

        postRequest(response -> {
            context.assertEquals(HttpResponseStatus.OK.code(), response.statusCode());

            response.bodyHandler(data -> {
                JsonArray characters = data.toJsonObject().getJsonArray("characters");
                context.assertTrue(characterInJsonArray(CHARACTER_NAME, characters));
                async.complete();
            });

        }, "/api/character-list").end(new JsonObject()
                .put("token", Serializer.json(getClientToken()))
                .put("realm", realmconfig.getName()).encode());
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
        Async async = context.async();

        postRequest(response -> {
            context.assertEquals(HttpResponseStatus.UNAUTHORIZED.code(), response.statusCode());
            async.complete();
        }, "/api/character-list").end(new JsonObject()
                .put("token", Serializer.json(getInvalidClientToken()))
                .put("realm", realmconfig.getName()).encode());
    }

    @Test
    public void realmDataOnCharacterList(TestContext context) {
        Async async = context.async();

        postRequest(response -> {
            context.assertEquals(HttpResponseStatus.OK.code(), response.statusCode());

            response.bodyHandler(data -> {
                JsonObject realm = data.toJsonObject().getJsonObject("realm");

                context.assertTrue(realm.containsKey("classes"));
                context.assertTrue(realm.containsKey("name"));
                context.assertTrue(realm.containsKey("afflictions"));
                context.assertTrue(realm.containsKey("template"));

                async.complete();
            });

        }, "/api/character-list").end(new JsonObject()
                .put("token", Serializer.json(getClientToken()))
                .put("realm", realmconfig.getName()).encode());
    }

    @Test
    public void realmDataDoesNotIncludeTokenOnCharacterList(TestContext context) {
        Async async = context.async();

        postRequest(response -> {
            context.assertEquals(HttpResponseStatus.OK.code(), response.statusCode());

            response.bodyHandler(data -> {
                JsonObject realm = data.toJsonObject().getJsonObject("realm");

                context.assertFalse(realm.containsKey("authentication"));
                context.assertFalse(realm.containsKey("token"));

                async.complete();
            });

        }, "/api/character-list").end(new JsonObject()
                .put("token", Serializer.json(getClientToken()))
                .put("realm", realmconfig.getName()).encode());
    }

    @Test
    public void createRealmToken(TestContext context) {
        Async async = context.async();

        postRequest(response -> {
            context.assertEquals(HttpResponseStatus.OK.code(), response.statusCode());

            response.bodyHandler(data -> {
                Token token = (Token) Serializer.unpack(data.toJsonObject(), Token.class);
                TokenFactory factory = new TokenFactory(realmconfig.getAuthentication().getToken().getKey().getBytes());

                context.assertEquals(USERNAME, token.getDomain());
                context.assertTrue(factory.verifyToken(token));

                async.complete();
            });

        }, "/api/realmtoken").end(new JsonObject()
                .put("token", Serializer.json(getClientToken()))
                .put("realm", realmconfig.getName()).encode());
    }

    @Test
    public void failCreateRealmTokenWhenInvalidToken(TestContext context) {
        Async async = context.async();

        postRequest(response -> {

            context.assertEquals(HttpResponseStatus.UNAUTHORIZED.code(), response.statusCode());
            async.complete();

        }, "/api/realmtoken").end(new JsonObject()
                .put("token", Serializer.json(getInvalidClientToken()))
                .put("realm", realmconfig.getName()).encode());
    }


}
