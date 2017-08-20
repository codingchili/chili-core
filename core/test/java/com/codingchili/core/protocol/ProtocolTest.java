package com.codingchili.core.protocol;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.listener.BaseRequest;
import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.exception.AuthorizationRequiredException;
import com.codingchili.core.protocol.exception.HandlerMissingException;
import com.codingchili.core.testing.EmptyRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.function.Consumer;

import static com.codingchili.core.protocol.Access.AUTHORIZED;
import static com.codingchili.core.protocol.Access.PUBLIC;
import static com.codingchili.core.protocol.ProtocolTest.AnnotatedRouter.*;

/**
 * @author Robin Duda
 *         <p>
 *         Tests the protocol class and its authorization mechanism.
 */
@RunWith(VertxUnitRunner.class)
public class ProtocolTest {
    private static final String TEST = "test";
    private static final String ANOTHER = "another";
    public static final String DOCSTRING_TEXT = "docstring text";
    private Protocol<RequestHandler<Request>> protocol;

    @Before
    public void setUp() {
        protocol = new Protocol<>();
    }

    @Test
    public void testHandlerMissing(TestContext test) throws Exception {
        Async async = test.async();

        try {
            protocol.use(TEST, Request::accept, PUBLIC)
                    .get(PUBLIC, ANOTHER).handle(new EmptyRequest());

            test.fail("Should throw handler missing exception.");
        } catch (HandlerMissingException e) {
            async.complete();
        }
    }

    @Test
    public void testPrivateRouteNoAccess(TestContext test) throws Exception {
        Async async = test.async();

        try {
            protocol.use(TEST, Request::accept, AUTHORIZED)
                    .get(PUBLIC, TEST).handle(new EmptyRequest());

            test.fail("Should throw authorization exception.");
        } catch (AuthorizationRequiredException e) {
            async.complete();
        }
    }

    @Test
    public void testPublicRouteWithAccess(TestContext test) throws Exception {
        Async async = test.async();

        protocol.use(TEST, Request::accept, PUBLIC)
                .get(PUBLIC, TEST).handle(new EmptyRequest() {
            @Override
            public void accept() {
                async.complete();
            }
        });
    }

    @Test
    public void testPrivateRoute(TestContext test) throws Exception {
        Async async = test.async();

        protocol.use(TEST, Request::accept, AUTHORIZED)
                .get(AUTHORIZED, TEST).handle(new EmptyRequest() {
            @Override
            public void accept() {
                async.complete();
            }
        });
    }

    @Test
    public void testPublicRoute(TestContext test) throws Exception {
        Async async = test.async();

        protocol.use(TEST, Request::accept, PUBLIC)
                .get(PUBLIC, TEST).handle(new EmptyRequest() {
            @Override
            public void accept() {
                async.complete();
            }
        });
    }

    @Test
    public void testAnnotatedPublic(TestContext test) {
        new AnnotatedRouter().handle(new TestRequest((response) -> {
            test.assertEquals(response, PUBLIC_ROUTE);
        }, PUBLIC_ROUTE, Access.PUBLIC));
    }

    @Test
    public void testAnnotatedPrivate(TestContext test) {
        try {
            new AnnotatedRouter().handle(new TestRequest((response) -> {
                test.assertEquals(response, PRIVATE_ROUTE);
            }, PRIVATE_ROUTE, Access.PUBLIC));
            test.fail("Unauthorized call did not fail.");
        } catch (AuthorizationRequiredException ignored) {
        }
    }

    @Test
    public void testAnnotatedDocumentation(TestContext test) {
        AnnotatedRouter router = new AnnotatedRouter();
        List<ProtocolMapping.ProtocolEntry> list = router.protocol.list().getRoutes();
        test.assertEquals(DOC_PRIVATE, list.get(0).getDocumentation());
        test.assertEquals(DOC_PUBLIC, list.get(1).getDocumentation());
    }

    @Test
    public void testCoreHandlerMissingAddress(TestContext test) {
        try {
            new AnnotatedRouterNoAddress().address();
            test.fail("Test case must fail when implementing class fails to provide address.");
        } catch (RuntimeException ignored) {
        }
    }

    @Test
    public void testCoreHandlerAnnotatedAddress(TestContext test) {
        test.assertEquals(new AnnotatedRouter().address(), ADDRESS);
    }

    @Test
    public void testListRoutes(TestContext test) {
        protocol.use(TEST, Request::accept, PUBLIC)
                .use(ANOTHER, Request::accept, AUTHORIZED);

        ProtocolMapping mapping = protocol.list();

        test.assertEquals(2, mapping.getRoutes().size());
        test.assertEquals(AUTHORIZED, mapping.getRoutes().get(0).getAccess());
        test.assertEquals(ANOTHER, mapping.getRoutes().get(0).getRoute());
        test.assertEquals(PUBLIC, mapping.getRoutes().get(1).getAccess());
        test.assertEquals(TEST, mapping.getRoutes().get(1).getRoute());
    }

    @Address(AnnotatedRouter.ADDRESS)
    public class AnnotatedRouter implements CoreHandler {
        static final String PUBLIC_ROUTE = "public";
        static final String PRIVATE_ROUTE = "private";
        static final String ADDRESS = "home";
        static final String DOC_PUBLIC = "a public route";
        static final String DOC_PRIVATE = "a private route";
        private Protocol<RequestHandler<Request>> protocol = new Protocol<>(this);

        @Public(value = PUBLIC_ROUTE, doc = DOC_PUBLIC)
        public void route(TestRequest request) {
            request.write(PUBLIC_ROUTE);
        }

        @Private(value = PRIVATE_ROUTE, doc = DOC_PRIVATE)
        public void route2(TestRequest request) {
            request.write(PRIVATE_ROUTE);
        }

        @Override
        public void handle(Request request) {
            protocol.get(((TestRequest) request).authorized(), request.route()).handle(request);
        }
    }

    // no @Address annotation and does not implement getAddress.
    public class AnnotatedRouterNoAddress implements CoreHandler {
        @Override
        public void handle(Request request) {
            //
        }
    }

    public class TestRequest extends BaseRequest {
        private Consumer<String> listener;
        private Access access;
        private String route;

        public TestRequest(Consumer<String> listener, String route, Access access) {
            this.listener = listener;
            this.route = route;
            this.access = access;
        }

        public Access authorized() {
            return access;
        }

        @Override
        public void write(Object object) {
            listener.accept(object.toString());
        }

        @Override
        public JsonObject data() {
            return new JsonObject().put(CoreStrings.PROTOCOL_ROUTE, route);
        }

        @Override
        public int timeout() {
            return 0;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public void init() {}
    }
}
