package com.codingchili.router.controller;

import com.codingchili.common.Strings;
import com.codingchili.core.context.CoreException;
import com.codingchili.core.listener.Request;

/**
 * @author Robin Duda
 *
 * Throw when a remote has failed to reply within the specified time.
 */
class RequestTimedOutException extends CoreException {
    RequestTimedOutException(Request request) {
        super(Strings.getTimeOutEcxeption(request.target(), request.timeout()));
    }
}
