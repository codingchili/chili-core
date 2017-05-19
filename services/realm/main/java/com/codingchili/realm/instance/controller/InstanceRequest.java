package com.codingchili.realm.instance.controller;

import com.codingchili.core.listener.Request;
import com.codingchili.core.listener.transport.ClusterRequest;

/**
 * @author Robin Duda
 */
class InstanceRequest extends ClusterRequest {

    InstanceRequest(Request request) {
        super(request);
    }

}
