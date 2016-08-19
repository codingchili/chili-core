package Protocols.Authentication;


import Configuration.Strings;
import Realm.Configuration.RealmSettings;
import Protocols.Header;

/**
 * @author Robin Duda
 * A request to register a realmName on the authentication server.
 */
public class RealmRegister {
    public static final String ACTION = Strings.REALM_REGISTER;
    private Header header;
    private RealmSettings realm;
    private Boolean registered;

    public RealmRegister() {
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
