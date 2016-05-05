package Protocol;


import Configuration.RealmSettings;

/**
 * Created by Robin on 2016-04-27.
 */
public class RegisterRealm {
    public static final String ACTION = "register.realm";
    private Header header;
    private RealmSettings realm;

    public RegisterRealm() {
        this.header = new Header(ACTION);
    }

    public RegisterRealm(RealmSettings realm) {
        this();
        this.realm = realm;
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
}
