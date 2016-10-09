package com.codingchili.core.Authentication.Controller;

import com.codingchili.core.Protocols.ClusterRequest;
import com.codingchili.core.Protocols.Request;
import com.codingchili.core.Protocols.Util.Serializer;
import com.codingchili.core.Protocols.Util.Token;
import com.codingchili.core.Realm.Configuration.RealmSettings;
import io.vertx.core.eventbus.Message;

import static com.codingchili.core.Configuration.Strings.*;

/**
 * @author Robin Duda
 */
class AuthenticationRequest extends ClusterRequest {

    AuthenticationRequest(Request request) {
        super(request);
    }

    public RealmSettings realm() {
        if (data().containsKey(ID_REALM)) {
            return Serializer.unpack(data().getJsonObject(ID_REALM), RealmSettings.class);
        } else {
            return new RealmSettings();
        }
    }

    public String realmName() {
        return token().getDomain();
    }

    public int players() {
        if (data().containsKey(ID_PLAYERS)) {
            return data().getInteger(ID_PLAYERS);
        } else {
            return 0;
        }
    }

    public String sender() {
        return data().getString(PROTOCOL_CONNECTION);
    }

    public String account() {
        return data().getString(ID_ACCOUNT);
    }

    public String name() {
        return data().getString(ID_CHARACTER);
    }
}
