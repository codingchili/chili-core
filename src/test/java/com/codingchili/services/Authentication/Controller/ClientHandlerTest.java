package com.codingchili.services.authentication.controller;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import com.codingchili.core.protocol.ResponseStatus;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.*;
import com.codingchili.core.testing.RequestMock;
import com.codingchili.core.testing.ResponseListener;

import com.codingchili.services.authentication.configuration.AuthContext;
import com.codingchili.services.authentication.model.*;
import com.codingchili.services.Shared.Strings;
import com.codingchili.services.realm.configuration.RealmSettings;
import com.codingchili.services.realm.instance.model.PlayerCharacter;
import com.codingchili.services.realm.instance.model.PlayerClass;

import static com.codingchili.services.Shared.Strings.*;

/**
 * @author Robin Duda
 *         tests the API from client -> authentication server.
 */

@RunWith(VertxUnitRunner.class)
public class ClientHandlerTest {
    private static final String PASSWORD = "password";
    private static final String USERNAME = "username";
    private static final String USERNAME_NEW = "new-username";
    private static final String USERNAME_MISSING = "missing-username";
    private static final String PASSWORD_WRONG = "wrong-password";
    private static final String REALM_NAME = "realmName.name";
    private static final String CLASS_NAME = "class.name";
    private static AuthContext context;
    private static ClientHandler handler;

    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);

    @Before
    public void setUp(TestContext test) throws IOException {
        RealmSettings realm = new RealmSettings()
                .setTemplate(new PlayerCharacter().setName("realm 1").setClassName(CLASS_NAME))
                .setAuthentication(new Token().setDomain(REALM_NAME))
                .setName(REALM_NAME)
                .setType("test-type")
                .setResources("ao.patching.node");

        ArrayList<PlayerClass> classes = new ArrayList<>();
        classes.add(new PlayerClass().setName(CLASS_NAME));

        realm.setClasses(classes);
        realm.setAuthentication(new Token().setKey("test-key"));

        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put(ID_DESCRIPTION, "Text description :) ");
        realm.setAttributes(attributes);

        context = new ContextMock(Vertx.vertx());
        context.getRealmStore().put(Future.future(), realm);

        handler = new ClientHandler<>(context);

        addAccount(test);
    }

    @After
    public void tearDown(TestContext test) {
        context.vertx().close(test.asyncAssertSuccess());
    }
    
    private static void addAccount(TestContext test) {
        Async async = test.async();
        AsyncAccountStore accounts = context.getAccountStore();

        Future<Account> future = Future.future();

        future.setHandler(result -> {
            async.complete();
        });

        accounts.register(future, new Account(USERNAME, PASSWORD));
    }


    @Test
    public void authenticateAccount(TestContext test) {
        Async async = test.async();

        handle(CLIENT_AUTHENTICATE, (response, status) -> {
            test.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        }, account(USERNAME, PASSWORD));
    }

    @Test
    public void failtoAuthenticateAccountWithWrongPassword(TestContext test) {
        Async async = test.async();

        handle(CLIENT_AUTHENTICATE, (response, status) -> {
            test.assertEquals(ResponseStatus.UNAUTHORIZED, status);
            async.complete();
        }, account(USERNAME, PASSWORD_WRONG));
    }

    @Test
    public void failtoAuthenticateAccountWithMissing(TestContext test) {
        Async async = test.async();

        handle(CLIENT_AUTHENTICATE, (response, status) -> {
            test.assertEquals(ResponseStatus.MISSING, status);
            async.complete();
        }, account(USERNAME_MISSING, PASSWORD));
    }

    @Test
    public void registerAccount(TestContext test) {
        Async async = test.async();

        handle(CLIENT_REGISTER, (response, status) -> {
            test.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        }, account(USERNAME_NEW, PASSWORD));
    }

    private JsonObject account(String username, String password) {
        return new JsonObject().put(ID_ACCOUNT, new JsonObject().put("username", username).put("password", password));
    }

    @Test
    public void failRegisterAccountExists(TestContext test) {
        Async async = test.async();

        handle(CLIENT_REGISTER, (response, status) -> {
            test.assertEquals(ResponseStatus.CONFLICT, status);
            async.complete();
        }, account(USERNAME, PASSWORD));
    }

    @Test
    public void retrieveRealmList(TestContext test) {
        Async async = test.async();
        String[] keys = {ID_NAME, ID_RESOURCES, ID_TYPE, ID_VERSION, ID_REMOTE, ID_PLAYERS, ID_SIZE, ID_ATTRIBUTES};

        handle(CLIENT_REALM_LIST, (response, status) -> {
            test.assertEquals(ResponseStatus.ACCEPTED, status);

            JsonArray list = response.getJsonArray(ID_REALMS);

            for (int i = 0; i < list.size(); i++) {
                JsonObject realm = list.getJsonObject(i);

                for (String key : keys)
                    test.assertTrue(realm.containsKey(key));
            }

            async.complete();
        });
    }

    private JsonObject getClientToken() {
        return Serializer.json(context.signClientToken(USERNAME));
    }

    @Test
    public void createRealmToken(TestContext test) {
        Async async = test.async();

        handle(CLIENT_REALM_TOKEN, (response, status) -> {
            Token token = Serializer.unpack(response, Token.class);

            test.assertEquals(ResponseStatus.ACCEPTED, status);
            test.assertEquals(USERNAME, token.getDomain());

            async.complete();
        }, new JsonObject()
                .put(ID_TOKEN, getClientToken())
                .put(ID_REALM, REALM_NAME));
    }

    @Test
    public void failCreateRealmTokenWhenInvalidToken(TestContext test) {
        handle(Strings.CLIENT_REALM_TOKEN, (response, status) -> {
            test.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        }, new JsonObject()
                .put(ID_TOKEN, getInvalidClientToken()));
    }

    @Test
    public void testPingClientHandler(TestContext test) {
        handle(ID_PING, ((response, status) -> {
            test.assertEquals(status, ResponseStatus.ACCEPTED);
        }));
    }

    private JsonObject getInvalidClientToken() {
        return Serializer.json(new Token(new TokenFactory("invalid".getBytes()), "username"));
    }

    private void handle(String action, ResponseListener listener) {
        handle(action, listener, new JsonObject().put(ID_TOKEN, getClientToken()));
    }

    private void handle(String action, ResponseListener listener, JsonObject data) {
        handler.process(RequestMock.get(action, listener, data));
    }
}
