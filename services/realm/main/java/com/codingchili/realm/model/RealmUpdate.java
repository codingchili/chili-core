package com.codingchili.realm.model;


import com.codingchili.realm.configuration.RealmSettings;

import com.codingchili.core.security.Token;

import static com.codingchili.common.Strings.REALM_UPDATE;

/**
 * @author Robin Duda
 *         A request to register a realm on the authentication server.
 */
public class RealmUpdate {
    private RealmSettings realm;
    private Token token;

    /**
     * @param realm constructs a new realm update from an existing realm.
     */
    public RealmUpdate(RealmSettings realm) {
        this.realm = realm;
        this.token = realm.getAuthentication();
    }

    public RealmSettings getRealm() {
        return realm;
    }

    public void setRealm(RealmSettings realm) {
        this.realm = realm;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public String getRoute() {
        return REALM_UPDATE;
    }
}
