package Protocol;


import Configuration.RealmSettings;
import Protocol.Header;
import Utilities.Token;

/**
 * @author Robin Duda
 * A request to register a realm on the authentication server.
 */
public class RegisterRealm {
    public static final String ACTION = "register.realm";
    private Header header;
    private RealmSettings realm;
    private Boolean registered;

    public RegisterRealm() {
        this.header = new Header(ACTION);
    }

    public RegisterRealm(RealmSettings realm) {
        this();
        this.realm = realm;
    }

    public RegisterRealm(boolean registered) {
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
