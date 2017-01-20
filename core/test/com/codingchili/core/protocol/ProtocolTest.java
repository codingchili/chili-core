package com.codingchili.core.protocol;

import io.vertx.core.Future;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.codingchili.core.protocol.exception.AuthorizationRequiredException;
import com.codingchili.core.protocol.exception.HandlerMissingException;
import com.codingchili.core.testing.EmptyRequest;
import com.codingchili.core.testing.RequestMock;

import static com.codingchili.core.protocol.Access.*;
import static com.codingchili.core.protocol.ResponseStatus.*;

/**
 * @author Robin Duda
 *
 * Tests the protocol class and its authorization mechanism.
 */
@RunWith(VertxUnitRunner.class)
public class ProtocolTest {
    private static final String TEST = "test";
    private static final String ANOTHER = "another";
    private Protocol<RequestHandler<Request>> protocol;

    @Before
    public void setUp() {
        protocol = new Protocol<>();

        // Add an exception handler.
        protocol.exception(Request::conflict, TestConflictException.class);
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

    @Test
    public void testHandleFailedFuture(TestContext test) {
        protocol.error(RequestMock.get((response, status) -> {
            test.assertEquals(CONFLICT, status);
        }), Future.failedFuture(new TestConflictException()));
    }

    @Test
    public void testHandleSucceededFuture() {
        protocol.error(RequestMock.get(((response, status) -> {})), Future.future());
    }

    @Test
    public void testHandleMappedException(TestContext test) {
        expectResultForException(CONFLICT, new TestConflictException(), test);
    }

    @Test
    public void testHandleUnmappedException(TestContext test) {
        expectResultForException(ERROR, new TestUnmappedException(), test);
    }

    @Test
    public void testHandleCatchAll(TestContext test) {
        protocol.exception(Request::unauthorized, Throwable.class);
        expectResultForException(UNAUTHORIZED, new TestCatchAllException(), test);
    }

    private void expectResultForException(ResponseStatus expected, Throwable exception, TestContext test) {
        protocol.error(RequestMock.get((response, status) -> test.assertEquals(expected, status)), exception);
    }

    /**
     * Test exception to test protocol exception handler.
     */
    class TestConflictException extends Throwable {}

    /**
     * Test for an unmapped exception, should use the default handler.
     */
    class TestUnmappedException extends Throwable {}

    /**
     * Test for the Throwable.class catchall handler.
     */
    class TestCatchAllException extends Throwable {}
}
