package com.codingchili.authentication.controller;

import com.codingchili.authentication.configuration.AuthenticationContext;
import com.codingchili.authentication.model.AsyncAccountStore;
import com.codingchili.authentication.model.ContextMock;
import com.codingchili.core.protocol.ResponseStatus;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.Account;
import com.codingchili.core.testing.RequestMock;
import com.codingchili.core.testing.ResponseListener;
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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.codingchili.common.Strings.*;

/**
 * @author Robin Duda
 * tests the API from client -> authentication server.
 */

@RunWith(VertxUnitRunner.class)
public class ClientHandlerTest {
    private static final String PASSWORD = "password";
    private static final String USERNAME = "username";
    private static final String USERNAME_NEW = "new-username";
    private static final String USERNAME_MISSING = "missing-username";
    private static final String PASSWORD_WRONG = "wrong-password";
    private static AuthenticationContext context;
    private static ClientHandler handler;

    @Rule
    public Timeout timeout = new Timeout(50, TimeUnit.SECONDS);

    private static void addAccount(TestContext test) {
        Async async = test.async();
        AsyncAccountStore accounts = context.getAccountStore();

        Future<Account> future = Future.future();

        future.setHandler(result -> {
            async.complete();
        });

        accounts.register(future, new Account(USERNAME, PASSWORD));
    }

    @Before
    public void setUp(TestContext test) throws IOException {
        context = new ContextMock();
        handler = new ClientHandler(context);
        addAccount(test);
    }

    @After
    public void tearDown(TestContext test) {
        context.close(test.asyncAssertSuccess());
    }

    @Test
    public void authenticateAccount(TestContext test) {
        Async async = test.async();

        handle(CLIENT_AUTHENTICATE, (response, status) -> {
            test.assertEquals(ResponseStatus.ACCEPTED, status);
            test.assertTrue(response.containsKey(ID_TOKEN));
            test.assertTrue(response.containsKey(ID_ACCOUNT));
            JsonObject account = response.getJsonObject(ID_ACCOUNT);
            test.assertFalse(account.containsKey(ID_PASSWORD));
            test.assertFalse(response.getBoolean(ID_REGISTERED));
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
            test.assertTrue(response.containsKey(ID_ACCOUNT));
            JsonObject account = response.getJsonObject(ID_ACCOUNT);
            test.assertFalse(account.containsKey(ID_PASSWORD));
            test.assertTrue(response.containsKey(ID_TOKEN));
            test.assertTrue(response.getBoolean(ID_REGISTERED));
            async.complete();
        }, account(USERNAME_NEW, PASSWORD));
    }

    private JsonObject account(String username, String password) {
        return new JsonObject().put(ID_ACCOUNT, new JsonObject()
                .put(ID_USERNAME, username)
                .put(ID_PASSWORD, password));
    }

    @Test
    public void failRegisterAccountExists(TestContext test) {
        Async async = test.async();

        handle(CLIENT_REGISTER, (response, status) -> {
            test.assertEquals(ResponseStatus.CONFLICT, status);
            async.complete();
        }, account(USERNAME, PASSWORD));
    }

    private JsonObject getClientToken() {
        return Serializer.json(context.signClientToken(USERNAME));
    }

    @Test
    public void testPingClientHandler(TestContext test) {
        handle(ID_PING, ((response, status) -> {
            test.assertEquals(status, ResponseStatus.ACCEPTED);
        }));
    }

    private void handle(String action, ResponseListener listener) {
        handle(action, listener, new JsonObject().put(ID_TOKEN, getClientToken()));
    }

    private void handle(String action, ResponseListener listener, JsonObject data) {
        handler.handle(RequestMock.get(action, listener, data));
    }
}
