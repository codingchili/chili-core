package com.codingchili.core.Authentication.Realm;

import com.codingchili.core.Authentication.Controller.AuthenticationHandler;
import com.codingchili.core.Authentication.Model.Account;
import com.codingchili.core.Authentication.Model.ProviderMock;
import com.codingchili.core.Protocols.ResponseStatus;
import com.codingchili.core.Protocols.Util.Serializer;
import com.codingchili.core.Protocols.Util.Token;
import com.codingchili.core.Protocols.Util.TokenFactory;
import com.codingchili.core.Realm.Configuration.RealmSettings;
import com.codingchili.core.Realm.Instance.Model.PlayerCharacter;
import com.codingchili.core.Shared.RequestMock;
import com.codingchili.core.Shared.ResponseListener;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static com.codingchili.core.Configuration.Strings.*;

/**
 * @author Robin Duda
 *         tests the API from realmName->authentication server.
 */

@RunWith(VertxUnitRunner.class)
public class ServerHandlerTest {
    private static final String ACCOUNT_NAME = "name";
    private static final String ACCOUNT_PASSWORD = "pass";
    private static final String CHARACTER_NAME = "character.name";
    private static final String REALM_NAME = "test-realm";
    private RealmSettings realmconfig = new RealmSettings();
    private AuthenticationHandler handler;
    private TokenFactory factory;
    private ProviderMock provider;

    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);

    @Before
    public void setUp() {
        provider = new ProviderMock();
        handler = new AuthenticationHandler(provider);
        factory = provider.getRealmTokenFactory();

        realmconfig.setName(REALM_NAME);
        realmconfig.getAuthentication().setToken(new Token(provider.getRealmTokenFactory(), REALM_NAME));
    }

    @Test
    public void registerRealmTest(TestContext context) {
        handle(REALM_REGISTER, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
        }, new JsonObject()
                .put(ID_ACTION, REALM_REGISTER)
                .put(ID_REALM, Serializer.json(realmconfig)));
    }

    @Test
    public void failRegisterRealmTest(TestContext context) {
        handle(REALM_REGISTER, (response, status) -> {
            context.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        });
    }

    @Test
    public void failWithClientToken(TestContext context) {
        Token token = new Token(provider.getClientTokenFactory(), realmconfig.getName());
        realmconfig.getAuthentication().setToken(token);

        handle(REALM_REGISTER, (response, status) -> {
            context.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        });

        realmconfig = new RealmSettings();
    }

    @Test
    public void updateRealmTest(TestContext context) {
        // need to register before updating.
        registerRealmTest(context);

        handle(REALM_UPDATE, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
        }, new JsonObject()
                .put(ID_ACTION, REALM_UPDATE)
                .put(ID_TOKEN, getToken())
                .put(ID_PLAYERS, 5));
    }

    @Test
    public void failUpdateRealmTest(TestContext context) {
        handle(REALM_UPDATE, ((response, status) -> {
            context.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        }));
    }

    @Test
    public void testClientClose(TestContext context) {
        // need to register realm before removing
        registerRealmTest(context);

        handle(CLIENT_CLOSE, ((response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
        }), new JsonObject()
                .put(ID_TOKEN, getToken()));
    }

    @Test
    public void failClientCloseMissingRealm(TestContext context) {
        handle(CLIENT_CLOSE, ((response, status) -> {
            context.assertEquals(ResponseStatus.ERROR, status);
        }), new JsonObject()
                .put(ID_TOKEN, getToken()));
    }

    @Test
    public void failRealmClose(TestContext context) {
        handle(CLIENT_CLOSE, (response, status) -> {
            context.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        });
    }

    @Test
    public void testRealmCharacterRequest(TestContext context) {
        Async async = context.async();
        // need to register before querying characters.
        registerRealmTest(context);

        Future<Account> future = Future.future();
        addAccountWithCharacter(future);

        future.setHandler(handler -> {
            handle(REALM_CHARACTER_REQUEST, (response, status) -> {
                context.assertEquals(ResponseStatus.ACCEPTED, status);
                context.assertEquals(CHARACTER_NAME, response.getJsonObject(ID_CHARACTER).getString(ID_NAME));
                async.complete();
            }, new JsonObject()
                    .put(ID_TOKEN, getToken())
                    .put(ID_ACCOUNT, ACCOUNT_NAME)
                    .put(ID_CHARACTER, CHARACTER_NAME));
        });
    }

    private void addAccountWithCharacter(Future<Account> future) {
        Account account = new Account()
                .setUsername(ACCOUNT_NAME)
                .setPassword(ACCOUNT_PASSWORD);

        HashMap<String, PlayerCharacter> characters = new HashMap<>();
        characters.put(CHARACTER_NAME, new PlayerCharacter().setName(CHARACTER_NAME));
        account.getCharacters().put(realmconfig.getName(), characters);

        provider.getAccountStore().register(future, account);
    }

    @Test
    public void testRealmCharacterRequestMissingAccount(TestContext context) {
        failGetCharactersWithParams(context, ACCOUNT_NAME + ".missing", CHARACTER_NAME);
    }

    @Test
    public void testRealmCharacterRequestMissingCharacter(TestContext context) {
        failGetCharactersWithParams(context, ACCOUNT_NAME, CHARACTER_NAME + ".missing");
    }

    private void failGetCharactersWithParams(TestContext context, String account, String character) {
        Async async = context.async();

        // need to register before querying characters.
        registerRealmTest(context);

        Future<Account> future = Future.future();
        addAccountWithCharacter(future);

        future.setHandler(handler -> {
            handle(REALM_CHARACTER_REQUEST, (response, status) -> {
                context.assertEquals(ResponseStatus.ERROR, status);
                async.complete();
            }, new JsonObject()
                    .put(ID_TOKEN, getToken())
                    .put(ID_ACCOUNT, ACCOUNT_NAME)
                    .put(ID_CHARACTER, CHARACTER_NAME + ".missing"));
        });
    }

    @Test
    public void testPingAuthenticationHandler(TestContext context) {
        handle(ID_PING, ((response, status) -> {
            context.assertEquals(status, ResponseStatus.ACCEPTED);
        }));
    }

    @Test
    public void failRealmCharacterRequest(TestContext context) {
        handle(REALM_CHARACTER_REQUEST, (response, status) -> {
            context.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        });
    }

    @Test
    public void failUpdateWhenInvalidToken(TestContext context) {
        handle(REALM_UPDATE, (response, status) -> {
            context.assertEquals(status, ResponseStatus.UNAUTHORIZED);
        });
    }

    @Test
    public void failCloseWhenInvalidToken(TestContext context) {
        handle(CLIENT_CLOSE, (response, status) -> {
            context.assertEquals(status, ResponseStatus.UNAUTHORIZED);
        });
    }

    @Test
    public void failCharacterRequestInvalidToken(TestContext context) {
        handle(REALM_CHARACTER_REQUEST, (response, status) -> {
            context.assertEquals(status, ResponseStatus.UNAUTHORIZED);
        });
    }

    private void handle(String action, ResponseListener listener) {
        handle(action, listener, null);
    }

    private void handle(String action, ResponseListener listener, JsonObject data) {
        handler.process(RequestMock.get(action, listener, data));
    }

    private JsonObject getToken() {
        return Serializer.json(new Token(factory, REALM_NAME));
    }
}
