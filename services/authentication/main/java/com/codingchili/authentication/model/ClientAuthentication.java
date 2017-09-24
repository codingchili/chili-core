package com.codingchili.authentication.model;

import com.codingchili.core.security.Account;
import com.codingchili.core.security.Token;

/**
 * @author Robin Duda
 * an authentication message from the server to the view.
 */
public class ClientAuthentication {
    private Token token;
    private Account account;
    private boolean registered;

    public ClientAuthentication() {
    }

    public ClientAuthentication(Account account, Token token, boolean registered) {
        this.account = account;
        this.token = token;
        this.registered = registered;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
