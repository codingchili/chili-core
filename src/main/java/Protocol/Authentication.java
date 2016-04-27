package Protocol;

import Authentication.Model.Account;
import Game.Model.RealmSettings;
import Utilities.Token;

import java.util.ArrayList;

/**
 * @author Robin Duda
 *         <p>
 *         An authentication message from the server to the view.
 */
public class Authentication {
    private ArrayList<RealmSettings> realms = new ArrayList<>();
    private Token token;
    private Account account;
    private boolean registered;

    public Authentication() {
    }

    public Authentication(Account account, Token token, boolean registered, ArrayList<RealmSettings> realms) {
        this.account = account;
        this.token = token;
        this.registered = registered;
        this.realms = realms;
    }

    public ArrayList<RealmSettings> getRealms() {
        return realms;
    }

    public void setRealms(ArrayList<RealmSettings> realms) {
        this.realms = realms;
    }

    public Token getToken() {
        return token;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
