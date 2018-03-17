package com.codingchili.realm.model;


import com.codingchili.core.security.Token;
import com.codingchili.realm.configuration.RealmSettings;

import static com.codingchili.common.Strings.REALM_UPDATE;

/**
 * @author Robin Duda
 * A request to register a realm on the authentication server.
 */
public class RealmUpdate {
    private RealmSettings realm;
    private Token token;
    private int players = 0;

    /**
     * @param realm constructs a new realm update from an existing realm.
     */
    public RealmUpdate(RealmSettings realm) {
        this.realm = realm;
        this.token = realm.getAuthentication();
    }

    public RealmUpdate setPlayers(int players) {
        this.players = players;
        return this;
    }

    public int getPlayers() {
        return players;
    }

    public RealmSettings getRealm() {
        return realm;
    }

    public RealmUpdate setRealm(RealmSettings realm) {
        this.realm = realm;
        return this;
    }

    public Token getToken() {
        return token;
    }

    public RealmUpdate setToken(Token token) {
        this.token = token;
        return this;
    }

    public String getRoute() {
        return REALM_UPDATE;
    }
}
