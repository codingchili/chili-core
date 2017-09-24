package com.codingchili.core.protocol.exception;

import com.codingchili.core.context.CoreException;
import com.codingchili.core.listener.Request;

import static com.codingchili.core.configuration.CoreStrings.getTimeOutEcxeption;

/**
 * @author Robin Duda
 * <p>
 * Throw when a remote has failed to reply within the specified time.
 */
public class RequestTimedOutException extends CoreException {
    public RequestTimedOutException(Request request) {
        super(getTimeOutEcxeption(request.target(), request.timeout()));
    }
}
