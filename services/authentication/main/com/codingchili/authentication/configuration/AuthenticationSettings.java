package com.codingchili.authentication.configuration;

import com.codingchili.common.Strings;

import com.codingchili.core.configuration.ServiceConfigurable;


public class AuthenticationSettings extends ServiceConfigurable {
    public static final String PATH_AUTHSERVER = Strings.getService("authserver");
    private byte[] clientSecret;

    public byte[] getClientSecret() {
        return clientSecret;
    }

    public AuthenticationSettings setClientSecret(byte[] secret) {
        this.clientSecret = secret;
        return this;
    }
}
