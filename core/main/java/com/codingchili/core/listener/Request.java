package com.codingchili.core.listener;

import com.codingchili.core.context.CoreException;
import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.context.exception.CoreExceptionFormat;
import com.codingchili.core.protocol.Protocol;
import com.codingchili.core.protocol.ResponseStatus;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.protocol.exception.UnmappedException;
import com.codingchili.core.security.Token;
import io.vertx.core.AsyncResult;
import io.vertx.core.impl.NoStackTraceThrowable;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;

import static com.codingchili.core.configuration.CoreStrings.*;
import static com.codingchili.core.listener.ListenerSettings.DEFAULT_MAX_REQUEST_BYTES;
import static com.codingchili.core.listener.ListenerSettings.DEFAULT_TIMEOUT;
import static com.codingchili.core.protocol.ResponseStatus.ACCEPTED;

/**
 * @author Robin Duda
 * <p>
 * Base request class.
 */
public interface Request {
    String TARGET_UNDEFINED = ID_UNDEFINED;

    /**
     * Called after construction of the request and before processing begins.
     * No data should be processed in the constructor since error messages cannot
     * be propagated to the client if the construction of the request fails.
     * <p>
     * All initial processing of data must be done in this method to allow
     * proper error handling.
     */
    default void init() {};

    /**
     * Writes an object to the connection that backs the current request.
     *
     * @param object the object to be written.
     */
    void write(Object object);

    /**
     * Accepts the request indicating that it was processed successfully
     * but that there is no response.
     */
    default void accept() {
        write(Protocol.response(ACCEPTED));
    }

    /**
     * Convenience method that converts the request to a future.
     * - if the future fails: Request::error is called with the cause
     * - if the future completes with null: Request::accept is called
     * - if the future completed with value: Request::write is called
     * @param event an event to be converted into a result that can be written to a request.
     */
    default void result(AsyncResult<?> event) {
        if (event.succeeded()) {
            if (event.result() != null) {
                write(event.result());
            } else {
                accept();
            }
        } else {
            error(event.cause());
        }
    }

    /**
     * Write an error and code to the response.
     *
     * @param exception the exception that caused the error.
     */
    default void error(Throwable exception) {
        if (exception instanceof CoreException || exception instanceof CoreRuntimeException) {
            write(Protocol.response(((CoreExceptionFormat) exception).status(), exception));
        } else if (exception instanceof DecodeException || exception instanceof NoStackTraceThrowable) {
            write(Protocol.response(ResponseStatus.ERROR, exception));
        } else {
            write(Protocol.response(ResponseStatus.ERROR, new UnmappedException(exception)));
        }
    }

    /**
     * Get the route from the request, the route specifies with method that should
     * be invoked.
     *
     * @return the requested route
     */
    default String route() {
        String route = data().getString(PROTOCOL_ROUTE);

        if (route == null) {
            route = DIR_SEPARATOR;
        }
        return route;
    }

    /**
     * The target node of the request. The target specifies which service or which
     * sub-service that the requested route resides in.
     *
     * @return the target node
     */
    default String target() {
        String target = data().getString(PROTOCOL_TARGET);

        if (target == null) {
            target = TARGET_UNDEFINED;
        }
        return target;
    }

    /**
     * Get the request token sent with the request.
     *
     * @return the requests token
     */
    default Token token() {
        if (data().containsKey(ID_TOKEN)) {
            return Serializer.unpack(data().getJsonObject(ID_TOKEN), Token.class);
        } else {
            return new Token();
        }
    }

    /**
     * Get the raw data of the request as a json object
     *
     * @return the raw data of the request
     */
    JsonObject data();

    /**
     * Get the request timeout which indicates how long the sender is waiting until
     * the request is considered to have timed out.
     *
     * defaults to #{@link ListenerSettings#DEFAULT_TIMEOUT}
     *
     * @return milliseconds specifying the timeout of the request
     */
    default int timeout() {
        return DEFAULT_TIMEOUT;
    }

    /**
     * @return the size of the request in bytes.
     */
    int size();

    /**
     * defaults to #{@link ListenerSettings#DEFAULT_MAX_REQUEST_BYTES}
     *
     * @return the maximum number of bytes allowed in a single request.
     */
    default int maxSize() {
        return DEFAULT_MAX_REQUEST_BYTES;
    }
}
