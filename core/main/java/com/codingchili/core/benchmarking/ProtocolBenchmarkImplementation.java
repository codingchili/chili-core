package com.codingchili.core.benchmarking;

import io.vertx.core.Future;

import java.lang.reflect.Method;

import com.codingchili.core.listener.Receiver;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.*;
import com.codingchili.core.testing.RequestMock;

/**
 * Benchmarks how the {@link Protocol} invokes the method of a route: {@link ProtocolDispatchStrategy#METHOD_INVOKE}
 * (the current implementation), {@link ProtocolDispatchStrategy#METHOD_HANDLE} and
 * {@link ProtocolDispatchStrategy#LAMBDA_METAFACTORY}. Two more implementations give the results context: a
 * direct call as a baseline, and {@link Protocol#process(Request)} end to end, which includes authentication,
 * route mapping and authorization.
 * <p>
 * Dispatching takes nanoseconds, so each iteration performs {@link #CALLS_PER_ITERATION} calls in a tight loop and
 * the reported rate is calls per second. The loops are shared by all implementations, as the call site that invokes a
 * route is shared by all routes in a protocol: all strategies are measured behind a call site that has seen
 * every one of them, and the order the implementations run in does not give any of them an advantage.
 * <p>
 * Registering routes is measured as well, as it is where the strategies differ the most in the opposite direction:
 * {@link ProtocolDispatchStrategy#LAMBDA_METAFACTORY} generates a class for each route. Each iteration binds both routes
 * of a new handler {@link #BINDS_PER_ITERATION} times.
 * <p>
 * After the run each implementation verifies that the handler received exactly the number of calls that were made.
 */
public class ProtocolBenchmarkImplementation extends BenchmarkImplementationBuilder {
    /**
     * The number of calls that are performed by one iteration.
     */
    public static final int CALLS_PER_ITERATION = 100_000;
    /**
     * The number of times the routes of a handler are bound by one iteration.
     */
    public static final int BINDS_PER_ITERATION = 20;
    public static final String WITH_REQUEST = "route with request";
    public static final String WITHOUT_REQUEST = "route without request";
    public static final String BIND = "register 2 routes";
    private static final String WITH_REQUEST_ROUTE = "withRequest";
    private static final String WITHOUT_REQUEST_ROUTE = "withoutRequest";
    private static final int PRIME_ROUNDS = 20;
    private static final int PRIME_CALLS = 1_000;
    private final Request request = RequestMock.get((response, status) -> {
    });
    private final DispatchHandler handler;
    private final RequestHandler<Request> withRequest;
    private final Runnable withoutRequest;
    private final Binding binding;
    private volatile Object bound;
    private Throwable bindFailure;
    private long expected = 0;

    /**
     * Binds the routes of a new handler, the way routes are registered in a protocol.
     */
    @FunctionalInterface
    public interface Binding {
        /**
         * @return what was created, so that it cannot be optimized away.
         * @throws Throwable if the routes cannot be bound.
         */
        Object bind() throws Throwable;
    }

    /**
     * Creates a benchmark of the given ways to invoke the routes of a handler.
     *
     * @param group          the group the implementation is part of.
     * @param name           the name of the implementation.
     * @param handler        the handler that counts the calls it receives.
     * @param withRequest    invokes the route with a request.
     * @param withoutRequest invokes the route without a request.
     * @param binding        binds the routes of a new handler.
     */
    public ProtocolBenchmarkImplementation(BenchmarkGroup group, String name, DispatchHandler handler,
                                           RequestHandler<Request> withRequest, Runnable withoutRequest,
                                           Binding binding) {
        super(name);
        setGroup(group);
        this.handler = handler;
        this.withRequest = withRequest;
        this.withoutRequest = withoutRequest;
        this.binding = binding;

        add(WITH_REQUEST, CALLS_PER_ITERATION, this::callWithRequest);
        add(WITHOUT_REQUEST, CALLS_PER_ITERATION, this::callWithoutRequest);
        add(BIND, BINDS_PER_ITERATION, this::bindRoutes);
    }

    /**
     * Creates the group of implementations that are compared: every {@link ProtocolDispatchStrategy},
     * a direct call and {@link Protocol#process(Request)}.
     *
     * @param iterations the number of iterations to run, an iteration is {@link #CALLS_PER_ITERATION} calls.
     * @return a group that is ready to be executed.
     * @throws Throwable if a route cannot be found on the handler, or cannot be bound by a strategy.
     */
    public static BenchmarkGroup group(int iterations) throws Throwable {
        BenchmarkGroup group = new BenchmarkGroupBuilder(
                String.format("Protocol dispatch (%,d calls per iteration)", CALLS_PER_ITERATION), iterations);

        Method withRequest = route(WITH_REQUEST_ROUTE, Request.class);
        Method withoutRequest = route(WITHOUT_REQUEST_ROUTE);

        for (ProtocolDispatchStrategy strategy : ProtocolDispatchStrategy.values()) {
            DispatchHandler handler = new DispatchHandler();
            group.add(new ProtocolBenchmarkImplementation(group, strategy.displayName(), handler,
                    strategy.bind(withRequest, handler),
                    strategy.bindWithoutRequest(withoutRequest, handler),
                    () -> {
                        DispatchHandler other = new DispatchHandler();
                        return new Object[]{strategy.bind(withRequest, other),
                                strategy.bindWithoutRequest(withoutRequest, other)};
                    }));
        }

        DispatchHandler direct = new DispatchHandler();
        group.add(new ProtocolBenchmarkImplementation(group, "Direct call (baseline)", direct,
                direct::withRequest, direct::withoutRequest,
                () -> {
                    DispatchHandler other = new DispatchHandler();
                    RequestHandler<Request> route = other::withRequest;
                    Runnable routeWithoutRequest = other::withoutRequest;
                    return new Object[]{route, routeWithoutRequest};
                }));

        DispatchHandler routed = new DispatchHandler();
        Protocol<Request> protocol = new Protocol<>(routed);
        Request processWithRequest = RequestMock.get(WITH_REQUEST_ROUTE, (response, status) -> {
        });
        Request processWithoutRequest = RequestMock.get(WITHOUT_REQUEST_ROUTE, (response, status) -> {
        });
        group.add(new ProtocolBenchmarkImplementation(group, "Protocol.process (end to end)", routed,
                request -> protocol.process(processWithRequest),
                () -> protocol.process(processWithoutRequest),
                () -> new Protocol<Request>(new DispatchHandler())));

        prime(group);
        return group;
    }

    private static Method route(String name, Class<?>... parameters) throws NoSuchMethodException {
        Method method = DispatchHandler.class.getDeclaredMethod(name, parameters);
        // as done by the protocol when routes are registered.
        method.setAccessible(true);
        return method;
    }

    /**
     * Runs every implementation through the shared call sites before anything is measured, so that
     * they all see the same call site: seen by one type only the JVM would inline it for the first
     * implementation that runs and not for the rest.
     */
    private static void prime(BenchmarkGroup group) {
        for (int round = 0; round < PRIME_ROUNDS; round++) {
            for (BenchmarkImplementation implementation : group.getImplementations()) {
                ((ProtocolBenchmarkImplementation) implementation).prime();
            }
        }
    }

    private void prime() {
        run(withRequest, request, PRIME_CALLS);
        run(withoutRequest, PRIME_CALLS);
        expected += 2L * PRIME_CALLS;
    }

    private Future<?> callWithRequest() {
        run(withRequest, request, CALLS_PER_ITERATION);
        expected += CALLS_PER_ITERATION;
        return Future.succeededFuture();
    }

    private Future<?> callWithoutRequest() {
        run(withoutRequest, CALLS_PER_ITERATION);
        expected += CALLS_PER_ITERATION;
        return Future.succeededFuture();
    }

    private Future<?> bindRoutes() {
        try {
            for (int i = 0; i < BINDS_PER_ITERATION; i++) {
                bound = binding.bind();
            }
        } catch (Throwable e) {
            bindFailure = e;
        }
        return Future.succeededFuture();
    }

    // the call sites below are shared by all implementations on purpose.
    private static void run(RequestHandler<Request> route, Request request, int calls) {
        for (int i = 0; i < calls; i++) {
            route.submit(request);
        }
    }

    private static void run(Runnable route, int calls) {
        for (int i = 0; i < calls; i++) {
            route.run();
        }
    }

    @Override
    public Future<Void> shutdown() {
        if (bindFailure != null) {
            return Future.failedFuture(bindFailure);
        } else if (handler.calls == expected) {
            return Future.succeededFuture();
        } else {
            return Future.failedFuture(new IllegalStateException(String.format(
                    "%s: the handler received %,d calls, expected %,d.", getName(), handler.calls, expected)));
        }
    }

    /**
     * The handler that is invoked, it counts the calls so that a strategy
     * that does not invoke the method it was bound to is detected.
     */
    @Roles(RoleMap.PUBLIC)
    public static class DispatchHandler implements Receiver<Request> {
        private long calls = 0;

        @Api
        public void withRequest(Request request) {
            calls++;
        }

        @Api
        public void withoutRequest() {
            calls++;
        }

        @Override
        public void handle(Request request) {
            // routes are invoked directly or through a protocol that is created for this handler.
        }
    }
}
