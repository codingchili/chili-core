package com.codingchili.realmregistry.model;

import com.codingchili.core.security.Token;

/**
 * @author Robin Duda
 */
public class TokenResponse {
    private Token token;

    public TokenResponse(Token token) {
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }
}
