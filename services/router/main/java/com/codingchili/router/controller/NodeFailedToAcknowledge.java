package com.codingchili.router.controller;

import com.codingchili.common.Strings;

import com.codingchili.core.context.CoreException;
import com.codingchili.core.protocol.Request;

/**
 * @author Robin Duda
 *         <p>
 *         Throw when the remote end has failed in some way.
 */
class NodeFailedToAcknowledge extends CoreException {
    /**
     * @param request the request that was failed to handle.
     */
    NodeFailedToAcknowledge(Request request) {
        super(Strings.getNodeFailedToAcknowledge(request.target(), request.route()));
    }
}
