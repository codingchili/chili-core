package com.codingchili.core.Protocols.Authentication;


import com.codingchili.core.Configuration.Strings;
import com.codingchili.core.Protocols.Header;
import com.codingchili.core.Realm.Configuration.RealmSettings;

/**
 * @author Robin Duda
 * A request to register a realmName on the authentication server.
 */
public class RealmRegister {
    public static final String ACTION = Strings.REALM_REGISTER;
    private Header header;
    private RealmSettings realm;
    private Boolean registered;

    private RealmRegister() {
        this.header = new Header(ACTION);
    }

    public RealmRegister(RealmSettings realm) {
        this();
        this.realm = realm;
    }

    public RealmRegister(boolean registered) {
        this();
        this.registered = registered;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public RealmSettings getRealm() {
        return realm;
    }


    public void setRealm(RealmSettings realm) {
        this.realm = realm;
    }

    public Boolean getRegistered() {
        return registered;
    }

    public void setRegistered(Boolean registered) {
        this.registered = registered;
    }
}
