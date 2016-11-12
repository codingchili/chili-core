package com.codingchili.services.Realm;

import io.vertx.core.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import com.codingchili.core.Protocol.ResponseStatus;
import com.codingchili.core.Protocol.Serializer;
import com.codingchili.core.Security.Token;
import com.codingchili.core.Security.TokenFactory;
import com.codingchili.core.Testing.RequestMock;
import com.codingchili.core.Testing.ResponseListener;

import com.codingchili.services.Realm.Controller.RealmHandler;
import com.codingchili.services.Realm.Instance.Model.PlayerCharacter;
import com.codingchili.services.Realm.Model.AsyncCharacterStore;
import com.codingchili.services.Shared.Strings;

import static com.codingchili.services.Shared.Strings.*;

/**
 * @author Robin Duda
 *         tests the API from client->realmName.
 */

@RunWith(VertxUnitRunner.class)
public class RealmHandlerTest {
    private static final String USERNAME = "username";
    private static final String CHARACTER_NAME_DELETED = "character-deleted";
    private static final String CHARACTER_NAME = "character";
    private static final String CLASS_NAME = "class.name";
    private AsyncCharacterStore characters;
    private TokenFactory clientToken;
    private RealmHandler handler;
    private Vertx vertx;

    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);

    @Before
    public void setUp(TestContext context) {
        Async async = context.async();
        vertx = Vertx.vertx();
        ContextMock provider = new ContextMock();
        handler = new RealmHandler<>(provider);
        clientToken = provider.getClientFactory();
        characters = provider.getCharacterStore();
        createCharacters(async);
    }

    private void createCharacters(Async async) {
        PlayerCharacter add = new PlayerCharacter().setName(CHARACTER_NAME);
        PlayerCharacter delete = new PlayerCharacter().setName(CHARACTER_NAME_DELETED);
        Future addFuture = Future.future();
        Future removeFuture = Future.future();

        CompositeFuture.all(addFuture, removeFuture).setHandler(done -> {
            async.complete();
        });

        characters.create(addFuture, USERNAME, add);
        characters.create(removeFuture, USERNAME, delete);
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void realmPingTest(TestContext context) {
        handle(ID_PING, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
        });
    }

    @Test
    public void removeCharacter(TestContext context) {
        Async async = context.async();

        handle(CLIENT_CHARACTER_REMOVE, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        }, new JsonObject()
                .put(ID_CHARACTER, CHARACTER_NAME_DELETED)
                .put(ID_TOKEN, getClientToken()));
    }

    @Test
    public void createCharacter(TestContext context) {
        Async async = context.async();

        handle(CLIENT_CHARACTER_CREATE, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            async.complete();
        }, new JsonObject()
                .put(ID_CHARACTER, CHARACTER_NAME + ".NEW")
                .put(ID_CLASS, CLASS_NAME)
                .put(ID_TOKEN, getClientToken()));
    }

    @Test
    public void failOverwriteExistingCharacter(TestContext context) {
        Async async = context.async();
        handle(CLIENT_CHARACTER_CREATE, (response, status) -> {
            context.assertEquals(ResponseStatus.CONFLICT, status);
            async.complete();
        }, new JsonObject()
                .put(ID_CHARACTER, CHARACTER_NAME)
                .put(ID_CLASS, CLASS_NAME)
                .put(ID_TOKEN, getClientToken()));
    }

    @Test
    public void failToRemoveMissingCharacter(TestContext context) {
        Async async = context.async();

        handle(CLIENT_CHARACTER_REMOVE, (response, status) -> {
            context.assertEquals(ResponseStatus.ERROR, status);
            async.complete();
        }, new JsonObject()
                .put(ID_CHARACTER, CHARACTER_NAME + ".MISSING")
                .put(ID_TOKEN, getClientToken()));
    }

    @Test
    public void listCharactersOnRealm(TestContext context) {
        Async async = context.async();

        handle(CLIENT_CHARACTER_LIST, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
            context.assertTrue(characterInJsonArray(CHARACTER_NAME, response.getJsonArray(ID_CHARACTERS)));
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
    public void realmDataOnCharacterList(TestContext context) {
        Async async = context.async();

        handle(CLIENT_CHARACTER_LIST, (response, status) -> {
            JsonObject realm = response.getJsonObject(ID_REALM);

            context.assertEquals(ResponseStatus.ACCEPTED, status);
            context.assertTrue(realm.containsKey(ID_CLASSES));
            context.assertTrue(realm.containsKey(ID_NAME));
            context.assertTrue(realm.containsKey(ID_AFFLICTIONS));
            context.assertTrue(realm.containsKey(ID_TEMPLATE));

            async.complete();
        }, new JsonObject()
                .put(ID_TOKEN, getClientToken()));
    }

    @Test
    public void realmDataDoesNotIncludeTokenOnCharacterList(TestContext context) {
        Async async = context.async();

        handle(CLIENT_CHARACTER_LIST, (response, status) -> {
            JsonObject realm = response.getJsonObject(ID_REALM);

            context.assertEquals(ResponseStatus.ACCEPTED, status);
            context.assertFalse(realm.containsKey(ID_AUTHENTICATION));
            context.assertFalse(realm.containsKey(ID_TOKEN));

            async.complete();
        }, new JsonObject()
                .put(ID_TOKEN, getClientToken()));
    }

    @Test
    public void failListCharactersOnRealmWhenInvalidToken(TestContext context) {
        handle(Strings.CLIENT_CHARACTER_LIST, (response, status) -> {
            context.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        }, new JsonObject()
                .put(ID_TOKEN, Serializer.json(getInvalidClientToken())));
    }

    @Test
    public void failToCreateCharacterWhenInvalidToken(TestContext context) {
        handle(Strings.CLIENT_CHARACTER_CREATE, (response, status) -> {
            context.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        }, new JsonObject()
                .put(ID_TOKEN, getInvalidClientToken()));
    }

    @Test
    public void failToRemoveCharacterWhenInvalidToken(TestContext context) {
        handle(CLIENT_CHARACTER_REMOVE, (response, status) -> {
            context.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        }, new JsonObject()
                .put(ID_TOKEN, getInvalidClientToken()));
    }

    private void handle(String action, ResponseListener listener) {
        handler.process(RequestMock.get(action, listener, null));
    }

    private void handle(String action, ResponseListener listener, JsonObject data) {
        handler.process(RequestMock.get(action, listener, data));
    }

    private JsonObject getInvalidClientToken() {
        return Serializer.json(new Token(new TokenFactory("invalid".getBytes()), "username"));
    }

    private JsonObject getClientToken() {
        return Serializer.json(new Token(clientToken, USERNAME));
    }
}
