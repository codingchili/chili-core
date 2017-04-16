package com.codingchili.realm.instance.controller;

import com.codingchili.core.listener.transport.ClusterRequest;
import com.codingchili.core.listener.Request;

/**
 * @author Robin Duda
 */
class InstanceRequest extends ClusterRequest {

    InstanceRequest(Request request) {
        super(request);
    }

}
