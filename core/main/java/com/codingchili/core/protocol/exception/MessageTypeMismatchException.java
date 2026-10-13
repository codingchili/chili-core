package com.codingchili.core.protocol.exception;

import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.protocol.ResponseStatus;

import static com.codingchili.core.configuration.CoreStrings.getMessageTypeMismatch;

/**
 * Thrown when a message is submitted to a route that cannot accept the type of the message, for example
 * when a route that takes a request is given a message that is not a request.
 */
public class MessageTypeMismatchException extends CoreRuntimeException {

    /**
     * @param route    the route that the message was submitted to.
     * @param expected the type the route accepts.
     * @param actual   the type of the message that was submitted, null if the message was null.
     */
    public MessageTypeMismatchException(String route, Class<?> expected, Class<?> actual) {
        super(getMessageTypeMismatch(route, expected.getName(), (actual == null) ? "null" : actual.getName()),
                ResponseStatus.ERROR);
    }
}
