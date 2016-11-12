package com.codingchili.services.Realm.Model;


import com.codingchili.core.Security.Token;

import com.codingchili.services.Realm.Configuration.RealmSettings;

import static com.codingchili.services.Shared.Strings.REALM_UPDATE;

/**
 * @author Robin Duda
 *         A request to register a realmName on the authentication server.
 */
public class RealmUpdate {
    private RealmSettings realm;
    private Token token;

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
