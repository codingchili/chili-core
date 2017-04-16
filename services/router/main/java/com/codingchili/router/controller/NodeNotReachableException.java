package com.codingchili.router.controller;

import com.codingchili.common.Strings;

import com.codingchili.core.context.CoreException;
import com.codingchili.core.listener.Request;

/**
 * @author Robin Duda
 *
 * Throw when a remote node is not in listening state. (not registered on the event bus)
 */
class NodeNotReachableException extends CoreException {
    NodeNotReachableException(Request request) {
        super(Strings.getNodeNotReachable(request.target()));
    }
}
