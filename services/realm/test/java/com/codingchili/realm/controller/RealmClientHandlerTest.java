package com.codingchili.realm.controller;

import com.codingchili.common.Strings;
import com.codingchili.realm.configuration.ContextMock;
import com.codingchili.realm.instance.model.entity.PlayerCreature;
import com.codingchili.realm.model.AsyncCharacterStore;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import com.codingchili.core.protocol.ResponseStatus;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.protocol.exception.AuthorizationRequiredException;
import com.codingchili.core.security.Token;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.testing.RequestMock;
import com.codingchili.core.testing.ResponseListener;

import static com.codingchili.common.Strings.*;

/**
 * @author Robin Duda
 * tests the API from client->realmName.
 */

@RunWith(VertxUnitRunner.class)
public class RealmClientHandlerTest {
    private static final String USERNAME = "username";
    private static final String CHARACTER_NAME_DELETED = "character-deleted";
    private static final String CHARACTER_NAME = "character";
    private static final String CLASS_NAME = "class.name";
    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);
    private AsyncCharacterStore characters;
    private TokenFactory clientToken;
    private RealmClientHandler handler;
    private ContextMock context;

    @Before
    public void setUp(TestContext test) {
        Async async = test.async();
        context = new ContextMock();
        handler = new RealmClientHandler(context);
        clientToken = context.getClientFactory();

        this.characters = context.characters();
        createCharacters(async);
        handler.start(Future.future());
    }

    @After
    public void tearDown(TestContext test) {
        context.close(test.asyncAssertSuccess());
    }

    private void createCharacters(Async async) {
        PlayerCreature add = new PlayerCreature(CHARACTER_NAME).setAccount(USERNAME);
        PlayerCreature delete = new PlayerCreature(CHARACTER_NAME_DELETED).setAccount(USERNAME);
        Future<Void> addFuture = Future.future();
        Future<Void> removeFuture = Future.future();

        CompositeFuture.all(addFuture, removeFuture).setHandler(done -> async.complete());

        characters.create(addFuture, add);
        characters.create(removeFuture, delete);
    }

    @Test
    public void realmPingTest(TestContext test) {
        handle(ID_PING, (response, status) -> {
            test.assertEquals(ResponseStatus.ACCEPTED, status);
        });
    }

    @Test
    public void removeCharacter(TestContext test) {
        Async async = test.async();

        handle(CLIENT_CHARACTER_REMOVE, (response, status) -> {
            test.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        }, new JsonObject()
                .put(ID_CHARACTER, CHARACTER_NAME_DELETED)
                .put(ID_TOKEN, getClientToken()));
    }

    @Test
    public void createCharacter(TestContext test) {
        Async async = test.async();

        handle(CLIENT_CHARACTER_CREATE, (response, status) -> {
            test.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        }, new JsonObject()
                .put(ID_CHARACTER, CHARACTER_NAME + ".NEW")
                .put(ID_CLASS, CLASS_NAME)
                .put(ID_TOKEN, getClientToken()));
    }

    @Test
    public void failOverwriteExistingCharacter(TestContext test) {
        Async async = test.async();
        handle(CLIENT_CHARACTER_CREATE, (response, status) -> {
            test.assertEquals(ResponseStatus.CONFLICT, status);
            async.complete();
        }, new JsonObject()
                .put(ID_CHARACTER, CHARACTER_NAME)
                .put(ID_CLASS, CLASS_NAME)
                .put(ID_TOKEN, getClientToken()));
    }

    @Test
    public void failToRemoveMissingCharacter(TestContext test) {
        Async async = test.async();

        handle(CLIENT_CHARACTER_REMOVE, (response, status) -> {
            test.assertEquals(ResponseStatus.ERROR, status);
            async.complete();
        }, new JsonObject()
                .put(ID_CHARACTER, CHARACTER_NAME + ".MISSING")
                .put(ID_TOKEN, getClientToken()));
    }

    @Test
    public void listCharactersOnRealm(TestContext test) {
        Async async = test.async();

        handle(CLIENT_CHARACTER_LIST, (response, status) -> {
            test.assertEquals(ResponseStatus.ACCEPTED, status);
            test.assertTrue(characterInJsonArray(CHARACTER_NAME, response.getJsonArray(ID_CHARACTERS)));
            async.complete();
        }, new JsonObject()
                .put(ID_TOKEN, getClientToken()));
    }

    private boolean characterInJsonArray(String charname, JsonArray characters) {
        Boolean found = false;

        for (int i = 0; i < characters.size(); i++) {
            if (characters.getJsonObject(i).getString(ID_NAME).equals(charname))
                found = true;
        }
        return found;
    }

    @Test
    public void realmDataOnCharacterList(TestContext test) {
        Async async = test.async();

        handle(CLIENT_CHARACTER_LIST, (response, status) -> {
            JsonObject realm = response.getJsonObject(ID_REALM);

            test.assertEquals(ResponseStatus.ACCEPTED, status);
            test.assertTrue(realm.containsKey(ID_NAME));
            test.assertTrue(realm.containsKey(ID_CLASSES));
            test.assertTrue(realm.containsKey(ID_AFFLICTIONS));

            async.complete();
        }, new JsonObject()
                .put(ID_TOKEN, getClientToken()));
    }

    @Test
    public void realmDataDoesNotIncludeTokenOnCharacterList(TestContext test) {
        Async async = test.async();

        handle(CLIENT_CHARACTER_LIST, (response, status) -> {
            JsonObject realm = response.getJsonObject(ID_REALM);

            test.assertEquals(ResponseStatus.ACCEPTED, status);
            test.assertFalse(realm.containsKey(ID_AUTHENTICATION));
            test.assertFalse(realm.containsKey(ID_TOKEN));

            async.complete();
        }, new JsonObject()
                .put(ID_TOKEN, getClientToken()));
    }

    @Test(expected = AuthorizationRequiredException.class)
    public void failListCharactersOnRealmWhenInvalidToken(TestContext test) {
        handle(Strings.CLIENT_CHARACTER_LIST, (response, status) -> {
            test.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        }, new JsonObject()
                .put(ID_TOKEN, Serializer.json(getInvalidClientToken())));
    }

    @Test(expected = AuthorizationRequiredException.class)
    public void failToCreateCharacterWhenInvalidToken(TestContext test) {
        handle(Strings.CLIENT_CHARACTER_CREATE, (response, status) -> {
            test.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        }, new JsonObject()
                .put(ID_TOKEN, getInvalidClientToken()));
    }

    @Test(expected = AuthorizationRequiredException.class)
    public void failToRemoveCharacterWhenInvalidToken(TestContext test) {
        handle(CLIENT_CHARACTER_REMOVE, (response, status) -> {
            test.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        }, new JsonObject()
                .put(ID_TOKEN, getInvalidClientToken()));
    }

    private void handle(String action, ResponseListener listener) {
        handler.handle(RequestMock.get(action, listener, null));
    }

    private void handle(String action, ResponseListener listener, JsonObject data) {
        handler.handle(RequestMock.get(action, listener, data));
    }

    private JsonObject getInvalidClientToken() {
        return Serializer.json(new Token(new TokenFactory("invalid".getBytes()), "username"));
    }

    private JsonObject getClientToken() {
        return Serializer.json(new Token(clientToken, USERNAME));
    }
}
