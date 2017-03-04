package com.codingchili.router.controller;

import com.codingchili.common.Strings;

import com.codingchili.core.context.CoreException;
import com.codingchili.core.protocol.Request;

/**
 * @author Robin Duda
 *
 * Throw when the remote end has failed in some way.
 */
class NodeFailedToAcknowledge extends CoreException {
    NodeFailedToAcknowledge(Request request) {
        super(Strings.getNodeFailedToAcknowledge(request.target()));
    }
}
