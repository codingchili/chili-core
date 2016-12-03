package com.codingchili.realmregistry.controller;

import com.codingchili.realmregistry.configuration.RealmSettings;

import com.codingchili.core.protocol.ClusterRequest;
import com.codingchili.core.protocol.Request;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.Token;

import static com.codingchili.common.Strings.*;

/**
 * @author Robin Duda
 */
class RealmRequest extends ClusterRequest {
    private RealmSettings realm;

    RealmRequest(Request request) {
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
