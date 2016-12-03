package com.codingchili.router.controller;

import com.codingchili.core.context.CoreException;

import com.codingchili.common.Strings;

/**
 * @author Robin Duda
 */
class RequestTimedOutException extends CoreException {
    RequestTimedOutException(String target, int timeout) {
        super(Strings.getTimeOutEcxeption(target, timeout));
    }
}
