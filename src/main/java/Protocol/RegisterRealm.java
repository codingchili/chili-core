package Protocol;


import Configuration.RealmSettings;
import Utilities.Token;

/**
 * Created by Robin on 2016-04-27.
 */
public class RegisterRealm {
    public static final String ACTION = "register.realm";
    private Header header;
    private RealmSettings realm;
    private Token token;

    public RegisterRealm() {
        this.header = new Header(ACTION);
    }

    public RegisterRealm(RealmSettings realm) {
        this();
        this.realm = realm;
        this.token = realm.getAuthentication().getToken();
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

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }
}
