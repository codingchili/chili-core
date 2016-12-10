package com.codingchili.core.protocol;

import com.codingchili.core.security.Token;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * @author Robin Duda
 *
 * Base request class, extend this class for new transports.
 */
public abstract class BaseRequest implements Request {
    private static final String UNDEFINED = "webserver.node";

    @Override
    public void accept() {
        send(ResponseStatus.ACCEPTED);
    }

    @Override
    public void error(Throwable exception) {
        send(ResponseStatus.ERROR, exception);
    }

    @Override
    public void unauthorized(Throwable exception) {
        send(ResponseStatus.UNAUTHORIZED);
    }

    @Override
    public void missing(Throwable exception) {
        send(ResponseStatus.MISSING, exception);
    }

    @Override
    public void conflict(Throwable exception) {
        send(ResponseStatus.CONFLICT, exception);
    }

    @Override
    public void bad(Throwable exception) {
        send(ResponseStatus.BAD, exception);
    }

    @Override
    public String route() {
        String route = data().getString(ID_ROUTE);

        if (route == null) {
            route = DIR_SEPARATOR;
        }
        return route;
    }

    @Override
    public String target() {
        String target = data().getString(ID_TARGET);

        if (target == null) {
            target = UNDEFINED;
        }
        return target;
    }

    @Override
    public Token token() {
        return Serializer.unpack(data().getJsonObject(ID_TOKEN), Token.class);
    }

    /**
     * sends a response to a request with an exception.
     * @param status the status of the message to send.
     * @param exception the exception that caused the abnormal status.
     */
    protected abstract void send(ResponseStatus status, Throwable exception);

    /**
     * sends a response to a request.
     * @param status the status of the message to send.
     */
    protected abstract void send(ResponseStatus status);
}
