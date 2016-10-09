package com.codingchili.core.Realm.Instance.Controller;

import com.codingchili.core.Protocols.ClusterRequest;
import com.codingchili.core.Protocols.Request;

/**
 * @author Robin Duda
 */
class InstanceRequest extends ClusterRequest {

    InstanceRequest(Request request) {
        super(request);
    }

}
