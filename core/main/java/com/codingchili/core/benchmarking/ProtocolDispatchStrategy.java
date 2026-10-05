package com.codingchili.core.benchmarking;

import java.lang.invoke.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.Protocol;
import com.codingchili.core.protocol.RequestHandler;

/**
 * The ways to invoke the method of a handler that are compared by the {@link ProtocolBenchmarkImplementation}.
 * <p>
 * A strategy binds a method to a handler instance once, as when a route is registered in a {@link Protocol},
 * and returns the code that invokes the method for every request. Exceptions thrown by the invoked method
 * are rethrown as they are: checked exceptions are not wrapped.
 */
public enum ProtocolDispatchStrategy {

    /**
     * {@link Method#invoke}: how the {@link Protocol} invokes routes.
     */
    METHOD_INVOKE("Method.invoke") {
        @Override
        public RequestHandler<Request> bind(Method method, Object handler) {
            return request -> {
                try {
                    method.invoke(handler, request);
                } catch (InvocationTargetException e) {
                    throw Protocol.throwAny(e.getCause());
                } catch (IllegalAccessException e) {
                    throw Protocol.throwAny(e);
                }
            };
        }

        @Override
        public Runnable bindWithoutRequest(Method method, Object handler) {
            return () -> {
                try {
                    method.invoke(handler);
                } catch (InvocationTargetException e) {
                    throw Protocol.throwAny(e.getCause());
                } catch (IllegalAccessException e) {
                    throw Protocol.throwAny(e);
                }
            };
        }
    },

    /**
     * A {@link MethodHandle} bound to the handler and adapted to take the request as an {@link Object}, so that a single
     * call site works for any request type. It is stored in a field and not in a constant, as routes are
     * registered at runtime: this means that the JVM cannot inline through the handle.
     */
    METHOD_HANDLE("MethodHandle") {
        @Override
        public RequestHandler<Request> bind(Method method, Object handler) throws ReflectiveOperationException {
            MethodHandle handle = lookup(method).unreflect(method).bindTo(handler)
                    .asType(MethodType.methodType(void.class, Object.class));

            return request -> {
                try {
                    handle.invokeExact((Object) request);
                } catch (Throwable e) {
                    throw Protocol.throwAny(e);
                }
            };
        }

        @Override
        public Runnable bindWithoutRequest(Method method, Object handler) throws ReflectiveOperationException {
            MethodHandle handle = lookup(method).unreflect(method).bindTo(handler)
                    .asType(MethodType.methodType(void.class));

            return () -> {
                try {
                    handle.invokeExact();
                } catch (Throwable e) {
                    throw Protocol.throwAny(e);
                }
            };
        }
    },

    /**
     * {@link LambdaMetafactory}: generates a class that implements {@link RequestHandler} (or {@link Runnable})
     * and calls the method directly, bound to the handler instance. The generated class is created when the route
     * is registered, which costs more than the other strategies.
     */
    LAMBDA_METAFACTORY("LambdaMetafactory") {
        @Override
        @SuppressWarnings("unchecked")
        public RequestHandler<Request> bind(Method method, Object handler) throws Throwable {
            MethodHandles.Lookup lookup = lookup(method);

            CallSite site = LambdaMetafactory.metafactory(lookup, "submit",
                    MethodType.methodType(RequestHandler.class, method.getDeclaringClass()),
                    MethodType.methodType(void.class, Object.class),
                    lookup.unreflect(method),
                    MethodType.methodType(void.class, method.getParameterTypes()[0]));

            return (RequestHandler<Request>) site.getTarget().invoke(handler);
        }

        @Override
        public Runnable bindWithoutRequest(Method method, Object handler) throws Throwable {
            MethodHandles.Lookup lookup = lookup(method);

            CallSite site = LambdaMetafactory.metafactory(lookup, "run",
                    MethodType.methodType(Runnable.class, method.getDeclaringClass()),
                    MethodType.methodType(void.class),
                    lookup.unreflect(method),
                    MethodType.methodType(void.class));

            return (Runnable) site.getTarget().invoke(handler);
        }
    };

    private final String displayName;

    ProtocolDispatchStrategy(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Binds a method that takes a request to a handler.
     *
     * @param method  the method to invoke, with a single request parameter.
     * @param handler the instance to invoke the method on.
     * @return code that invokes the method with a request.
     * @throws Throwable if the method cannot be bound.
     */
    public abstract RequestHandler<Request> bind(Method method, Object handler) throws Throwable;

    /**
     * Binds a method without parameters to a handler.
     *
     * @param method  the method to invoke.
     * @param handler the instance to invoke the method on.
     * @return code that invokes the method.
     * @throws Throwable if the method cannot be bound.
     */
    public abstract Runnable bindWithoutRequest(Method method, Object handler) throws Throwable;

    /**
     * @return the name used in reports.
     */
    public String displayName() {
        return displayName;
    }

    /**
     * Handlers may declare their routes as private or package private methods.
     *
     * @param method the method to access.
     * @return a lookup with access to the declaring class of the method.
     */
    private static MethodHandles.Lookup lookup(Method method) throws IllegalAccessException {
        return MethodHandles.privateLookupIn(method.getDeclaringClass(), MethodHandles.lookup());
    }
}
