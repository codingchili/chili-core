package com.codingchili.services.Router.Controller;

import com.codingchili.core.Exception.CoreException;

import com.codingchili.services.Shared.Strings;

/**
 * @author Robin Duda
 */
class RequestTimedOutException extends CoreException {
    RequestTimedOutException(String target, int timeout) {
        super(Strings.getTimeOutEcxeption(target, timeout));
    }
}
