package com.codingchili.realmregistry.controller;

import com.codingchili.common.Strings;
import com.codingchili.realmregistry.ContextMock;
import com.codingchili.realmregistry.model.RealmList;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.codingchili.core.protocol.ResponseStatus;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.Token;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.testing.RequestMock;
import com.codingchili.core.testing.ResponseListener;

import static com.codingchili.common.Strings.*;

/**
 * @author Robin Duda
 *         tests the API from client -> authentication server.
 */

@RunWith(VertxUnitRunner.class)
public class ClientHandlerTest {
    private static final String USERNAME = "username";
    private static final String REALM_NAME = "realmName";
    private ClientHandler handler;
    private ContextMock context;

    @Rule
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

    @Before
    public void setUp() throws IOException {
        context = new ContextMock(Vertx.vertx());
        handler = new ClientHandler<>(context);
    }

    @After
    public void tearDown(TestContext test) {
        context.vertx().close(test.asyncAssertSuccess());
    }

    @Test
    public void listRealms(TestContext test) {
        Async async = test.async();

        handle(CLIENT_REALM_LIST, (response, status) -> {
            RealmList realms = Serializer.unpack(response, RealmList.class);

            test.assertEquals(ResponseStatus.ACCEPTED, status);
            test.assertNotNull(realms);
            test.assertFalse(realms.getRealms().isEmpty());

            async.complete();
        });
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

    private JsonObject getClientToken() {
        return Serializer.json(new Token(context.getClientFactory(), USERNAME));
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
