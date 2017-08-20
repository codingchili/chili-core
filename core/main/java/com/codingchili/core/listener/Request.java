package com.codingchili.core.listener;

import com.codingchili.core.security.Token;
import io.vertx.core.AsyncResult;
import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 *         <p>
 *         Base request class.
 */
public interface Request {

    /**
     * Called after construction of the request and before processing begins.
     * No data should be processed in the constructor since error messages cannot
     * be propagated to the client if the construction of the request fails.
     *
     * All initial processing of data must be done in this method to allow
     * proper error handling.
     */
    void init();

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
    void accept();

    /**
     * Convenience method that converts the request to a future.
     * - if the future fails: Request::error is called with the cause
     * - if the future completes with null: Request::accept is called
     * - if the future completed with value: Request::write is called
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
    void error(Throwable exception);

    /**
     * Get the route from the request, the route specifies with method that should
     * be invoked.
     *
     * @return the requested route
     */
    String route();

    /**
     * The target node of the request. The target specifies which service or which
     * sub-service that the requested route resides in.
     *
     * @return the target node
     */
    String target();

    /**
     * Get the request token sent with the request.
     *
     * @return the requests token
     */
    Token token();

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
     * @return milliseconds specifying the timeout of the request
     */
    int timeout();

    /**
     * @return the size of the request in bytes.
     */
    int size();

    /**
     * @return the maximum number of bytes allowed in a single request.
     */
    int maxSize();
}
