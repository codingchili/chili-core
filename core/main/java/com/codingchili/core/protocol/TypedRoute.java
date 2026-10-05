package com.codingchili.core.protocol;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import tools.jackson.core.JacksonException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.exception.RequestValidationException;

/**
 * <b>Experimental.</b> Routes with typed input and output: a method that takes the input of the route as a parameter
 * and returns its result, instead of reading the request and writing the response.
 * <pre>{@code
 * @Api
 * public Future<AccountView> get(GetAccount input) {
 *     return accounts.get(input.id()).map(AccountView::of);
 * }
 * }</pre>
 * A method is a typed route when its first parameter is not a {@link Request}. The method may take the request as a
 * second parameter, for the token or the connection.
 * <ul>
 * <li>The input is deserialized from the data of the request. A record can validate itself in its constructor:
 * a constructor that throws {@link IllegalArgumentException} is answered with {@link ResponseStatus#BAD} and the
 * message. The data of the request contains the route, the target and the token as well, the input type ignores
 * what it doesn't declare.</li>
 * <li>A returned {@link Future} is written when it succeeds, and answered as an error when it fails. A returned value
 * is written. A method that returns {@code void}, or a {@code Future<Void>}, or null, answers with
 * {@link ResponseStatus#ACCEPTED}.</li>
 * <li>An exception that is thrown by the method, or that fails the future, is answered as an error as in any other route:
 * exceptions of the framework with their status, others as {@link ResponseStatus#ERROR}.</li>
 * <li>The class of the input is used as the model in the documentation of the route.</li>
 * </ul>
 */
final class TypedRoute {
    private TypedRoute() {
    }

    /**
     * @param method a method annotated as a route.
     * @return true if the method is a typed route.
     */
    static boolean applies(Method method) {
        return method.getParameterCount() > 0 && !Request.class.isAssignableFrom(method.getParameterTypes()[0]);
    }

    /**
     * @param method a typed route.
     * @return the type of the input.
     */
    static Class<?> input(Method method) {
        return method.getParameterTypes()[0];
    }

    /**
     * Creates the handler of a typed route.
     *
     * @param instance the handler that the route is a method of.
     * @param method   the method, must be a typed route and accessible.
     * @param <R>      the type of the request that the protocol handles.
     * @return a request handler that reads the input, invokes the method and writes the result.
     */
    static <R> RequestHandler<R> handler(Object instance, Method method) {
        Class<?> input = input(method);
        boolean withRequest = method.getParameterCount() > 1;

        if (method.getParameterCount() > 2 || (withRequest && !Request.class.isAssignableFrom(method.getParameterTypes()[1]))) {
            throw new IllegalArgumentException(method + ": a typed route takes the input, and optionally the request.");
        }

        return wrapped -> {
            Request request = (Request) wrapped;
            Object value;

            try {
                Object parsed = read(request.data(), input);
                // the request is passed as it was given to the protocol, which may be a wrapper of the request.
                value = (withRequest) ? method.invoke(instance, parsed, wrapped) : method.invoke(instance, parsed);
            } catch (InvocationTargetException e) {
                request.error(e.getCause());
                return;
            } catch (RequestValidationException e) {
                request.error(e);
                return;
            } catch (ReflectiveOperationException e) {
                request.error(e);
                return;
            }
            write(request, value);
        };
    }

    private static void write(Request request, Object value) {
        if (value instanceof Future<?> future) {
            future.onSuccess(result -> respond(request, result)).onFailure(request::error);
        } else {
            respond(request, value);
        }
    }

    private static void respond(Request request, Object result) {
        if (result == null || result instanceof Void) {
            request.accept();
        } else {
            request.write(result);
        }
    }

    /**
     * Reads the input of a route.
     *
     * @throws RequestValidationException if the data is not valid for the type, with the reason.
     */
    static Object read(JsonObject data, Class<?> type) {
        try {
            return Serializer.json.convertValue(data, type);
        } catch (JacksonException e) {
            throw new RequestValidationException(reason(e));
        }
    }

    /**
     * A constructor that validates throws {@link IllegalArgumentException}: its message is for the client. Anything else
     * that goes wrong is a mismatch between the data and the type, which is not a message that should be shown as it is.
     */
    private static String reason(JacksonException e) {
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (cause instanceof IllegalArgumentException invalid && !(cause instanceof JacksonException)) {
                return invalid.getMessage();
            }
        }
        return "invalid input: " + e.getOriginalMessage();
    }

    /**
     * @param method a typed route.
     * @return the type that a route returns in a future, or the return type of the method.
     */
    static Type result(Method method) {
        Type type = method.getGenericReturnType();

        if (type instanceof ParameterizedType parameterized && Future.class.equals(parameterized.getRawType())) {
            return parameterized.getActualTypeArguments()[0];
        }
        return type;
    }
}
