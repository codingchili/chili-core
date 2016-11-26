package com.codingchili.core.security;

/**
 * @author Robin Duda
 *
 *
 */
public class TokenIdentifier {
    private String service;
    private String secret;

    public TokenIdentifier() {}

    public TokenIdentifier(String service, String secret) {
        this.service = service;
        this.secret = secret;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
