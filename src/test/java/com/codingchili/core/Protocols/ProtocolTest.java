package com.codingchili.core.Protocols;

import com.codingchili.core.Protocols.Exception.AuthorizationRequiredException;
import com.codingchili.core.Protocols.Exception.HandlerMissingException;
import com.codingchili.core.Protocols.Util.Protocol;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.codingchili.core.Protocols.Access.AUTHORIZED;
import static com.codingchili.core.Protocols.Access.PUBLIC;

/**
 * @author Robin Duda
 */
@RunWith(VertxUnitRunner.class)
public class ProtocolTest {
    private Protocol<RequestHandler<Request>> protocol;

    @Before
    public void setUp() {
        protocol = new Protocol<>();
    }

    @Test
    public void testHandlerMissing(TestContext context) throws Exception {
        Async async = context.async();

        try {
            protocol.use("test", Request::accept, PUBLIC)
                    .get(PUBLIC, "another").handle(new MockRequest());

            context.fail("Should throw handler missing exception.");
        } catch (HandlerMissingException e) {
            async.complete();
        }
    }

    @Test
    public void testPrivateRouteNoAccess(TestContext context) throws Exception {
        Async async = context.async();

        try {
            protocol.use("test", Request::accept, AUTHORIZED)
                    .get(PUBLIC, "test").handle(new MockRequest());

            context.fail("Should throw authorization exception.");
        } catch (AuthorizationRequiredException e) {
            async.complete();
        }
    }

    @Test
    public void testPublicRouteWithAccess(TestContext context) throws Exception {
        Async async = context.async();

        protocol.use("test", Request::accept, PUBLIC)
                .get(PUBLIC, "test").handle(new MockRequest() {
            @Override
            public void accept() {
                async.complete();
            }
        });
    }

    @Test
    public void testPrivateRoute(TestContext context) throws Exception {
        Async async = context.async();

        protocol.use("test", Request::accept, AUTHORIZED)
                .get(AUTHORIZED, "test").handle(new MockRequest() {
            @Override
            public void accept() {
                async.complete();
            }
        });
    }

    @Test
    public void testPublicRoute(TestContext context) throws Exception {
        Async async = context.async();

        protocol.use("test", Request::accept, PUBLIC)
                .get(PUBLIC, "test").handle(new MockRequest() {
            @Override
            public void accept() {
                async.complete();
            }
        });
    }
}
