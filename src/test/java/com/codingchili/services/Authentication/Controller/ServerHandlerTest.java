package com.codingchili.services.authentication.controller;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import com.codingchili.core.protocol.ResponseStatus;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.Token;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.testing.RequestMock;
import com.codingchili.core.testing.ResponseListener;

import com.codingchili.services.authentication.model.ContextMock;
import com.codingchili.services.realm.configuration.RealmSettings;

import static com.codingchili.services.Shared.Strings.*;


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
    private ContextMock mock;

    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);

    @Before
    public void setUp() {
        mock = new ContextMock(Vertx.vertx());
        handler = new AuthenticationHandler<>(mock);
        factory = mock.getRealmFactory();

        realmconfig.setAuthentication(new Token(mock.getRealmFactory(), REALM_NAME));
        realmconfig.setName(REALM_NAME);
    }

    @After
    public void tearDown(TestContext test) {
        mock.vertx().close(test.asyncAssertSuccess());
    }

    @Test
    public void failRegisterRealmTest(TestContext context) {
        handle(REALM_UPDATE, (response, status) -> {
            context.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        });
    }

    @Test
    public void failWithClientToken(TestContext context) {
        Token token = new Token(mock.getClientFactory(), realmconfig.getName());
        realmconfig.setAuthentication(token);

        handle(REALM_UPDATE, (response, status) -> {
            context.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        });

        realmconfig = new RealmSettings();
    }

    @Test
    public void updateRealmTest(TestContext context) {
        handle(REALM_UPDATE, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
        }, new JsonObject()
                .put(ID_REALM, Serializer.json(realmconfig))
                .put(ID_TOKEN, Serializer.json(realmconfig.getAuthentication())));
    }

    @Test
    public void failUpdateRealmTest(TestContext context) {
        handle(REALM_UPDATE, (response, status) -> {
            context.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        });
    }

    @Test
    public void testClientClose(TestContext context) {
        // need to register realm before removing
        updateRealmTest(context);

        handle(CLIENT_CLOSE, (response, status) -> {
            context.assertEquals(ResponseStatus.ACCEPTED, status);
        }, new JsonObject()
                .put(ID_REALM, Serializer.json(realmconfig))
                .put(ID_TOKEN, Serializer.json(realmconfig.getAuthentication())));
    }

    @Test
    public void failClientCloseMissingRealm(TestContext context) {
        handle(CLIENT_CLOSE, (response, status) -> {
            context.assertEquals(ResponseStatus.ERROR, status);
        }, new JsonObject()
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
        handle(ID_PING, (response, status) -> {
            context.assertEquals(status, ResponseStatus.ACCEPTED);
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
