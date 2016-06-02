package Protocols.Authentication;


import Configuration.Gameserver.RealmSettings;
import Protocols.Header;

/**
 * @author Robin Duda
 * A request to register a realmName on the authentication server.
 */
public class RealmRegister {
    public static final String ACTION = "realm.register";
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
