package com.codingchili.core.protocol;

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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.listener.Receiver;
import com.codingchili.core.listener.Request;
import com.codingchili.core.listener.RequestWrapper;
import com.codingchili.core.storage.exception.ValueMissingException;
import com.codingchili.core.testing.RequestMock;
import com.codingchili.core.testing.ResponseListener;

import static com.codingchili.core.configuration.CoreStrings.PROTOCOL_MESSAGE;
import static com.codingchili.core.protocol.ResponseStatus.*;

/**
 * A sample of typed routes: a handler where the methods take their input and return their result, and how the
 * protocol answers when the input is invalid or the route fails. See {@link TypedRoute}.
 */
@RunWith(VertxUnitRunner.class)
public class TypedRouteTest {
    private AccountHandler handler;

    @Rule
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

    @Before
    public void setUp() {
        handler = new AccountHandler();
    }

    // The sample: the types of the api.

    /**
     * A record validates its input in its constructor, an IllegalArgumentException is answered with BAD.
     */
    record GetAccount(String id) {
        GetAccount {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("id is required");
            }
        }
    }

    record CreateAccount(String name, int level) {
        CreateAccount {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("name is required");
            }
            if (level < 0 || level > 100) {
                throw new IllegalArgumentException("level must be 0-100");
            }
        }
    }

    record RenameAccount(String id, String name) {
    }

    record AccountView(String id, String name, int level) {
    }

    /**
     * The sample: compare with a handler that reads the request and writes the response.
     */
    @Roles(RoleMap.PUBLIC)
    @Description("Accounts, a sample of typed routes.")
    public static class AccountHandler implements Receiver<Request> {
        private final Map<String, AccountView> accounts = new ConcurrentHashMap<>();
        private final Protocol<Request> protocol = new Protocol<>(this);

        // returns a future: written when it succeeds, answered as an error when it fails.
        @Api
        @Description("Get an account.")
        public Future<AccountView> get(GetAccount input) {
            AccountView account = accounts.get(input.id());
            return (account == null) ?
                    Future.failedFuture(new ValueMissingException(input.id())) :
                    Future.succeededFuture(account);
        }

        // takes the request as well: for the token or the connection.
        @Api
        public Future<AccountView> create(CreateAccount input, Request request) {
            AccountView account = new AccountView(UUID.randomUUID().toString(), input.name(), input.level());
            accounts.put(account.id(), account);
            return Future.succeededFuture(account);
        }

        // returns a value.
        @Api
        public AccountView rename(RenameAccount input) {
            return accounts.compute(input.id(), (id, account) -> new AccountView(id, input.name(), account.level()));
        }

        // void: answered with accepted.
        @Api
        public void delete(GetAccount input) {
            accounts.remove(input.id());
        }

        @Api(RoleMap.ADMIN)
        public Future<Void> purge(GetAccount input) {
            accounts.clear();
            return Future.succeededFuture();
        }

        @Api
        public AccountView explode(GetAccount input) {
            throw new IllegalStateException("boom");
        }

        @Api
        public Future<AccountView> conflict(GetAccount input) {
            return Future.failedFuture(new CoreRuntimeException("exists", CONFLICT));
        }

        // a route that reads the request and writes the response, in the same handler.
        @Api
        public void legacy(Request request) {
            request.write(new JsonObject().put("legacy", true));
        }

        @Override
        public void handle(Request request) {
            protocol.process(request);
        }
    }

    // The tests.

    @Test
    public void testFutureIsWrittenWhenItSucceeds(TestContext test) {
        AccountView account = new AccountView("1", "robin", 5);
        handler.accounts.put("1", account);

        call(test, "get", new JsonObject().put("id", "1"), (json, status) -> {
            test.assertEquals(ACCEPTED, status);
            test.assertEquals("robin", json.getString("name"));
            test.assertEquals(5, json.getInteger("level"));
        });
    }

    @Test
    public void testFutureFailureIsAnsweredWithItsStatus(TestContext test) {
        call(test, "get", new JsonObject().put("id", "missing"), (json, status) ->
                test.assertEquals(MISSING, status));
    }

    @Test
    public void testFailureWithAnotherStatus(TestContext test) {
        call(test, "conflict", new JsonObject().put("id", "1"), (json, status) -> {
            test.assertEquals(CONFLICT, status);
            test.assertEquals("exists", json.getString(PROTOCOL_MESSAGE));
        });
    }

    @Test
    public void testInputIsValidatedByTheRecord(TestContext test) {
        call(test, "get", new JsonObject(), (json, status) -> {
            test.assertEquals(BAD, status);
            test.assertEquals("id is required", json.getString(PROTOCOL_MESSAGE));
        });
    }

    @Test
    public void testInputOutOfRange(TestContext test) {
        call(test, "create", new JsonObject().put("name", "robin").put("level", 101), (json, status) -> {
            test.assertEquals(BAD, status);
            test.assertEquals("level must be 0-100", json.getString(PROTOCOL_MESSAGE));
        });
    }

    @Test
    public void testInputOfTheWrongType(TestContext test) {
        call(test, "create", new JsonObject().put("name", "robin").put("level", "high"), (json, status) -> {
            test.assertEquals(BAD, status);
            // the reason of the mismatch is not an internal message: it names the problem.
            test.assertTrue(json.getString(PROTOCOL_MESSAGE).startsWith("invalid input: "));
        });
    }

    @Test
    public void testTheEnvelopeOfTheRequestIsNotPartOfTheInput(TestContext test) {
        // route, target and token share the object with the input, and are ignored by the type.
        JsonObject data = new JsonObject().put("name", "robin").put("level", 5)
                .put("token", new JsonObject().put("domain", "x").put("key", "y").put("expiry", 0));

        call(test, "create", data, (json, status) -> {
            test.assertEquals(ACCEPTED, status);
            test.assertEquals("robin", json.getString("name"));
        });
    }

    @Test
    public void testRequestAsSecondParameter(TestContext test) {
        call(test, "create", new JsonObject().put("name", "robin").put("level", 1), (json, status) -> {
            test.assertEquals(ACCEPTED, status);
            test.assertNotNull(json.getString("id"));
            test.assertEquals(1, handler.accounts.size());
        });
    }

    @Test
    public void testReturnedValueIsWritten(TestContext test) {
        handler.accounts.put("1", new AccountView("1", "robin", 5));

        call(test, "rename", new JsonObject().put("id", "1").put("name", "chili"), (json, status) -> {
            test.assertEquals(ACCEPTED, status);
            test.assertEquals("chili", json.getString("name"));
            test.assertEquals(5, json.getInteger("level"));
        });
    }

    @Test
    public void testVoidIsAnsweredWithAccepted(TestContext test) {
        handler.accounts.put("1", new AccountView("1", "robin", 5));

        call(test, "delete", new JsonObject().put("id", "1"), (json, status) -> {
            test.assertEquals(ACCEPTED, status);
            test.assertTrue(handler.accounts.isEmpty());
        });
    }

    @Test
    public void testFutureOfVoidIsAnsweredWithAccepted(TestContext test) {
        // authorized by the role of the route: the authenticator of the protocol decides the role of the request.
        handler.protocol.authenticator(request -> Future.succeededFuture(Role.ADMIN));

        call(test, "purge", new JsonObject().put("id", "1"), (json, status) ->
                test.assertEquals(ACCEPTED, status));
    }

    @Test
    public void testRolesApplyToTypedRoutes(TestContext test) {
        // purge requires the admin role, the authenticator of the protocol answers with the public role.
        call(test, "purge", new JsonObject().put("id", "1"), (json, status) ->
                test.assertEquals(UNAUTHORIZED, status));
    }

    @Test
    public void testExceptionIsAnsweredAsAnError(TestContext test) {
        call(test, "explode", new JsonObject().put("id", "1"), (json, status) ->
                test.assertEquals(ERROR, status));
    }

    @Test
    public void testRoutesThatReadTheRequestStillWork(TestContext test) {
        call(test, "legacy", new JsonObject(), (json, status) -> {
            test.assertEquals(ACCEPTED, status);
            test.assertTrue(json.getBoolean("legacy"));
        });
    }

    @Test
    public void testTheTypeOfTheInputDocumentsTheRoute(TestContext test) {
        Map<String, String> model = handler.protocol.getSchema().getRoutes().get("create").getModel();

        test.assertEquals(Map.of("name", "java.lang.String", "level", "int"), model);
        test.assertEquals("java.lang.String", handler.protocol.getSchema().getRoutes().get("get").getModel().get("id"));
    }

    @Test
    public void testStringValuesAreCoercedAsInAQueryString(TestContext test) {
        // rest requests merge the query string into the data: every value is a string.
        call(test, "create", new JsonObject().put("name", "robin").put("level", "7"), (json, status) -> {
            test.assertEquals(ACCEPTED, status);
            test.assertEquals(7, json.getInteger("level"));
        });
    }

    /**
     * A request that wraps another request, as handlers do to add what is specific to the application.
     */
    static class AccountRequest implements RequestWrapper {
        private final Request request;

        AccountRequest(Request request) {
            this.request = request;
        }

        @Override
        public Request request() {
            return request;
        }

        String owner() {
            return "owner-of-" + target();
        }
    }

    @Roles(RoleMap.PUBLIC)
    public static class WrappedHandler implements Receiver<AccountRequest> {
        private final Protocol<AccountRequest> protocol = new Protocol<>(this);
        private String owner;

        // the second parameter is the request that was given to the protocol: the wrapper.
        @Api
        public void who(GetAccount input, AccountRequest request) {
            owner = request.owner();
        }

        @Override
        public void handle(AccountRequest request) {
            protocol.process(request);
        }
    }

    @Test
    public void testRequestParameterIsTheWrapperThatTheProtocolWasGiven(TestContext test) {
        WrappedHandler wrapped = new WrappedHandler();
        Async async = test.async();

        wrapped.handle(new AccountRequest(RequestMock.get("who", (json, status) -> {
            test.assertEquals(ACCEPTED, status);
            test.assertTrue(wrapped.owner.startsWith("owner-of-"));
            async.complete();
        }, new JsonObject().put("id", "1"))));
    }

    @Test
    public void testTypedRouteMustTakeTheRequestSecond(TestContext test) {
        try {
            new Protocol<Request>().annotated(new Receiver<Request>() {
                @Api
                public void route(GetAccount input, String other) {
                }

                @Override
                public void handle(Request request) {
                }
            });
            test.fail("expected a route with an unsupported signature to be rejected.");
        } catch (IllegalArgumentException e) {
            test.assertTrue(e.getMessage().contains("takes the input, and optionally the request"));
        }
    }

    private void call(TestContext test, String route, JsonObject data, ResponseListener listener) {
        Async async = test.async();

        handler.handle(RequestMock.get(route, (json, status) -> {
            listener.handle(json, status);
            async.complete();
        }, data));
    }
}
