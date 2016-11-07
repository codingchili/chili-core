package com.codingchili.core.Security;

/**
 * @author Robin Duda
 */
public class TokenIdentifier {
    private String service;
    private String secret;

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
