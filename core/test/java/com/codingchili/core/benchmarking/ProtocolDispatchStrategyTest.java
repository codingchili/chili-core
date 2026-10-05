package com.codingchili.core.benchmarking;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

import com.codingchili.core.listener.Request;
import com.codingchili.core.listener.transport.ClusterRequest;
import com.codingchili.core.protocol.RequestHandler;
import com.codingchili.core.testing.RequestMock;

/**
 * Verifies that every strategy for invoking a route does the same thing, so that the benchmark compares equals.
 */
@RunWith(Parameterized.class)
public class ProtocolDispatchStrategyTest {
    private final ProtocolDispatchStrategy strategy;
    private final Request request = RequestMock.get((response, status) -> {
    });
    private Handler handler;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<ProtocolDispatchStrategy> strategies() {
        return Arrays.asList(ProtocolDispatchStrategy.values());
    }

    public ProtocolDispatchStrategyTest(ProtocolDispatchStrategy strategy) {
        this.strategy = strategy;
    }

    @Before
    public void setUp() {
        handler = new Handler();
    }

    @Test
    public void invokesTheRouteWithTheRequest() throws Throwable {
        strategy.bind(route("one", Request.class), handler).submit(request);

        Assert.assertEquals(1, handler.calls);
        Assert.assertSame(request, handler.last);
    }

    @Test
    public void invokesTheRouteWithoutRequest() throws Throwable {
        strategy.bindWithoutRequest(route("none"), handler).run();

        Assert.assertEquals(1, handler.calls);
    }

    @Test
    public void invokesTheRouteOfTheBoundHandlerOnly() throws Throwable {
        Handler other = new Handler();
        RequestHandler<Request> route = strategy.bind(route("one", Request.class), handler);
        strategy.bind(route("one", Request.class), other);

        route.submit(request);
        route.submit(request);

        Assert.assertEquals(2, handler.calls);
        Assert.assertEquals(0, other.calls);
    }

    @Test
    public void invokesPrivateRoutesOfPackagePrivateHandlers() throws Throwable {
        strategy.bind(route("secret", Request.class), handler).submit(request);

        Assert.assertEquals(1, handler.calls);
        Assert.assertSame(request, handler.last);
    }

    @Test
    public void invokesRoutesThatTakeASubtypeOfRequest() throws Throwable {
        strategy.bind(route("typed", ClusterRequest.class), handler).submit(request);

        Assert.assertEquals(1, handler.calls);
        Assert.assertSame(request, handler.last);
    }

    @Test
    public void rethrowsCheckedExceptionsAsTheyAre() throws Throwable {
        RequestHandler<Request> route = strategy.bind(route("checked", Request.class), handler);

        try {
            route.submit(request);
            Assert.fail("expected the exception of the route.");
        } catch (Throwable e) {
            Assert.assertEquals(Exception.class, e.getClass());
            Assert.assertEquals("checked", e.getMessage());
        }
    }

    @Test
    public void rethrowsUncheckedExceptionsAsTheyAre() throws Throwable {
        RequestHandler<Request> route = strategy.bind(route("unchecked", Request.class), handler);

        try {
            route.submit(request);
            Assert.fail("expected the exception of the route.");
        } catch (IllegalStateException e) {
            Assert.assertEquals("unchecked", e.getMessage());
        }
    }

    private static Method route(String name, Class<?>... parameters) throws NoSuchMethodException {
        Method method = Handler.class.getDeclaredMethod(name, parameters);
        // as done by the protocol when routes are registered.
        method.setAccessible(true);
        return method;
    }

    /**
     * A package private handler, routes are not required to be public.
     */
    static class Handler {
        int calls = 0;
        Request last;

        public void one(Request request) {
            calls++;
            last = request;
        }

        public void none() {
            calls++;
        }

        private void secret(Request request) {
            calls++;
            last = request;
        }

        public void typed(ClusterRequest request) {
            calls++;
            last = request;
        }

        public void checked(Request request) throws Exception {
            throw new Exception("checked");
        }

        public void unchecked(Request request) {
            throw new IllegalStateException("unchecked");
        }
    }
}
