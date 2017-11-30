package com.codingchili.core.listener;

import com.codingchili.core.context.CoreException;
import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.context.exception.CoreExceptionFormat;
import com.codingchili.core.protocol.Protocol;
import com.codingchili.core.protocol.ResponseStatus;
import com.codingchili.core.protocol.exception.UnmappedException;
import io.vertx.core.AsyncResult;
import io.vertx.core.impl.NoStackTraceThrowable;
import io.vertx.core.json.DecodeException;

import static com.codingchili.core.protocol.ResponseStatus.ACCEPTED;

/**
 * Defines a set of operations supported by the messaging implementations.
 */
public interface Messageable {

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

}
