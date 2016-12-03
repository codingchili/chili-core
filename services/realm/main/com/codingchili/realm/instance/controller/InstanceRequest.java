package com.codingchili.realm.instance.controller;

import com.codingchili.core.protocol.ClusterRequest;
import com.codingchili.core.protocol.Request;

/**
 * @author Robin Duda
 */
class InstanceRequest extends ClusterRequest {

    InstanceRequest(Request request) {
        super(request);
    }

}
