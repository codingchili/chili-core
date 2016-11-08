package com.codingchili.services.Authentication.Controller;

import com.codingchili.core.Protocol.ClusterRequest;
import com.codingchili.core.Protocol.Request;
import com.codingchili.core.Protocol.Serializer;
import com.codingchili.core.Security.Token;

import com.codingchili.services.Realm.Configuration.RealmSettings;

import static com.codingchili.services.Shared.Strings.*;

/**
 * @author Robin Duda
 */
class AuthenticationRequest extends ClusterRequest {
    private RealmSettings realm;

    AuthenticationRequest(Request request) {
        super(request);

        parseRealm();
    }

    private void parseRealm() {
        if (data().containsKey(ID_REALM)) {
            realm = Serializer.unpack(data().getJsonObject(ID_REALM), RealmSettings.class);
        } else {
            realm = new RealmSettings();
        }
    }

    public Token token() {
        return realm.getAuthentication();
    }

    public RealmSettings getRealm() {
        return realm;
    }

    public String realmName() {
        return realm.getName();
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