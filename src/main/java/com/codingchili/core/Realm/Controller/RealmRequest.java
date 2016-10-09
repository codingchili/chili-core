package com.codingchili.core.Realm.Controller;

import com.codingchili.core.Protocols.ClusterRequest;
import com.codingchili.core.Protocols.Realm.CharacterRequest;
import com.codingchili.core.Protocols.Request;
import com.codingchili.core.Protocols.Util.Serializer;
import io.vertx.core.eventbus.Message;

/**
 * @author Robin Duda
 */
class RealmRequest extends ClusterRequest {

    RealmRequest(Request request) {
        super(request);
    }

    CharacterRequest characterRequest() {
        return Serializer.unpack(data(), CharacterRequest.class);
    }
}
