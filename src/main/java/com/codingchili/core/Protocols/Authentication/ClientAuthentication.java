package com.codingchili.core.Protocols.Authentication;

import com.codingchili.core.Authentication.Model.Account;
import com.codingchili.core.Protocols.Util.Token;

import java.util.ArrayList;

/**
 * @author Robin Duda
 *         an authentication message from the server to the view.
 */
public class ClientAuthentication {
    private ArrayList<RealmMetaData> realms = new ArrayList<>();
    private Token token;
    private Account account;
    private boolean registered;

    public ClientAuthentication() {
    }

    public ClientAuthentication(Account account, Token token, boolean registered, ArrayList<RealmMetaData> realms) {
        this.account = account;
        this.token = token;
        this.registered = registered;
        this.realms = realms;
    }

    public ArrayList<RealmMetaData> getRealms() {
        return realms;
    }

    public void setRealms(ArrayList<RealmMetaData> realms) {
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
