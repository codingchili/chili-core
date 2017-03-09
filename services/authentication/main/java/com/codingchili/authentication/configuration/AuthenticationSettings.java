package com.codingchili.authentication.configuration;

import com.codingchili.common.Strings;

import com.codingchili.core.configuration.ServiceConfigurable;
import com.codingchili.core.storage.IndexedMap;


public class AuthenticationSettings extends ServiceConfigurable {
    public static final String PATH_AUTHSERVER = Strings.getService("authserver");
    private byte[] clientSecret;
    private String storage = IndexedMap.class.getName();

    public byte[] getClientSecret() {
        return clientSecret;
    }

    public AuthenticationSettings setClientSecret(byte[] secret) {
        this.clientSecret = secret;
        return this;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }
}
