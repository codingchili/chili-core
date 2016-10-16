package com.codingchili.core.Authentication.Realm;

import com.codingchili.core.Authentication.Controller.AuthenticationHandler;
import com.codingchili.core.Authentication.Model.ProviderMock;
import com.codingchili.core.Protocols.ResponseStatus;
import com.codingchili.core.Protocols.Util.Serializer;
import com.codingchili.core.Protocols.Util.Token;
import com.codingchili.core.Protocols.Util.TokenFactory;
import com.codingchili.core.Realm.Configuration.RealmSettings;
import com.codingchili.core.Shared.RequestMock;
import com.codingchili.core.Shared.ResponseListener;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static com.codingchili.core.Configuration.Strings.*;

/**
 * @author Robin Duda
 *         tests the API from realmName->authentication server.
 */

@RunWith(VertxUnitRunner.class)
public class ServerHandlerTest {
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
    public void testPingAuthenticationHandler(TestContext context) {
        handle(ID_PING, ((response, status) -> {
            context.assertEquals(status, ResponseStatus.ACCEPTED);
        }));
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
