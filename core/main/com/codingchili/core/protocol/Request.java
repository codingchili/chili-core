package com.codingchili.core.protocol;

import io.vertx.core.json.JsonObject;

import com.codingchili.core.security.Token;

/**
 * @author Robin Duda
 *         <p>
 *         Base request class.
 */
public interface Request {

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
}
