package com.codingchili.core.protocol.exception;

import com.codingchili.core.context.CoreException;
import com.codingchili.core.listener.Request;

import static com.codingchili.core.configuration.CoreStrings.getNodeNotReachable;

/**
 * @author Robin Duda
 *
 * Throw when a remote node is not in listening state. (not registered on the event bus)
 */
public class NodeNotReachableException extends CoreException {
    public NodeNotReachableException(Request request) {
        super(getNodeNotReachable(request.target()));
    }
}
