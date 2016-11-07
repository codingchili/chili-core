package com.codingchili.services.Realm.Instance.Controller;

import com.codingchili.core.Protocol.ClusterRequest;
import com.codingchili.core.Protocol.Request;

/**
 * @author Robin Duda
 */
class InstanceRequest extends ClusterRequest {

    InstanceRequest(Request request) {
        super(request);
    }

}
