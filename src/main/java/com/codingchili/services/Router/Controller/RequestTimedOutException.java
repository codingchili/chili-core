package com.codingchili.services.router.controller;

import com.codingchili.core.context.CoreException;

import com.codingchili.services.Shared.Strings;

/**
 * @author Robin Duda
 */
class RequestTimedOutException extends CoreException {
    RequestTimedOutException(String target, int timeout) {
        super(Strings.getTimeOutEcxeption(target, timeout));
    }
}
