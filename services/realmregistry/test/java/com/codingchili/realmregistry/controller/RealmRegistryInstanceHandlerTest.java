package com.codingchili.realmregistry.controller;

import com.codingchili.core.protocol.ResponseStatus;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.protocol.exception.AuthorizationRequiredException;
import com.codingchili.core.security.Token;
import com.codingchili.core.testing.RequestMock;
import com.codingchili.core.testing.ResponseListener;
import com.codingchili.realmregistry.ContextMock;
import com.codingchili.realmregistry.configuration.RegisteredRealm;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static com.codingchili.common.Strings.*;


/**
 * @author Robin Duda
 * tests the API from realmName->authentication server.
 */

@RunWith(VertxUnitRunner.class)
public class RealmRegistryInstanceHandlerTest {
    private static final String REALM_NAME = "test-realm";
    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);
    private RegisteredRealm realmconfig = new RegisteredRealm();
    private RealmRegistryInstanceHandler handler;
    private ContextMock mock;

    @Before
    public void setUp(TestContext test) {
        Async async = test.async();
        mock = new ContextMock();
        handler = new RealmRegistryInstanceHandler(mock);
        realmconfig.setAuthentication(new Token(mock.getRealmFactory(), REALM_NAME));
        realmconfig.setName(REALM_NAME);

        Future<Void> future = Future.future();
        handler.start(future);
        future.setHandler(done -> async.complete());
    }

    @After
    public void tearDown(TestContext test) {
        mock.close(test.asyncAssertSuccess());
    }

    @Test(expected = AuthorizationRequiredException.class)
    public void failRegisterRealmTest(TestContext test) {
        handle(REALM_UPDATE, (response, status) -> {
            test.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        });
    }

    @Test(expected = AuthorizationRequiredException.class)
    public void failWithClientToken(TestContext test) {
        Token token = new Token(mock.getClientFactory(), realmconfig.getName());
        realmconfig.setAuthentication(token);

        handle(REALM_UPDATE, (response, status) -> {
            test.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        });

        realmconfig = new RegisteredRealm();
    }

    @Test
    public void updateRealmTest(TestContext test) {
        handle(REALM_UPDATE, (response, status) -> {
            test.assertEquals(ResponseStatus.ACCEPTED, status);
        }, new JsonObject()
                .put(ID_REALM, Serializer.json(realmconfig))
                .put(ID_TOKEN, getToken()));
    }

    @Test(expected = AuthorizationRequiredException.class)
    public void failUpdateRealmTest(TestContext test) {
        handle(REALM_UPDATE, (response, status) -> {
            test.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        });
    }

    @Test
    public void testClientClose(TestContext test) {
        // need to register realm before removing
        updateRealmTest(test);

        handle(CLIENT_CLOSE, (response, status) -> {
            test.assertEquals(ResponseStatus.ACCEPTED, status);
        }, new JsonObject()
                .put(ID_REALM, Serializer.json(realmconfig))
                .put(ID_TOKEN, Serializer.json(realmconfig.getAuthentication())));
    }

    @Test
    public void failClientCloseMissingRealm(TestContext test) {
        handle(CLIENT_CLOSE, (response, status) -> {
            test.assertEquals(ResponseStatus.ERROR, status);
        }, new JsonObject()
                .put(ID_TOKEN, getToken()));
    }

    @Test(expected = AuthorizationRequiredException.class)
    public void failRealmClose(TestContext test) {
        handle(CLIENT_CLOSE, (response, status) -> {
            test.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        });
    }

    @Test
    public void testPingAuthenticationHandler(TestContext test) {
        handle(ID_PING, (response, status) -> {
            test.assertEquals(status, ResponseStatus.ACCEPTED);
        });
    }

    @Test(expected = AuthorizationRequiredException.class)
    public void failUpdateWhenInvalidToken(TestContext test) {
        handle(REALM_UPDATE, (response, status) -> {
            test.assertEquals(status, ResponseStatus.UNAUTHORIZED);
        });
    }

    @Test(expected = AuthorizationRequiredException.class)
    public void failCloseWhenInvalidToken(TestContext test) {
        handle(CLIENT_CLOSE, (response, status) -> {
            test.assertEquals(status, ResponseStatus.UNAUTHORIZED);
        });
    }

    private void handle(String action, ResponseListener listener) {
        handle(action, listener, null);
    }

    private void handle(String action, ResponseListener listener, JsonObject data) {
        handler.handle(RequestMock.get(action, listener, data));
    }

    private JsonObject getToken() {
        return Serializer.json(new Token(mock.getRealmFactory(), REALM_NAME));
    }
}
