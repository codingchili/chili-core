package com.codingchili.core.protocol.exception;

import com.codingchili.core.context.CoreException;
import com.codingchili.core.listener.Request;

import static com.codingchili.core.configuration.CoreStrings.getNodeFailedToAcknowledge;

/**
 * Throw when the remote end has failed in some way.
 */
public class NodeFailedToAcknowledge extends CoreException {
    /**
     * @param request the request that was failed to handle.
     */
    public NodeFailedToAcknowledge(Request request) {
        super(getNodeFailedToAcknowledge(request.target(), request.route()));
    }
}
