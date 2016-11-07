package com.codingchili.services.Authentication.Configuration;

import java.util.ArrayList;
import java.util.List;

import com.codingchili.core.Configuration.ServiceConfigurable;
import com.codingchili.services.Shared.Strings;


public class AuthServerSettings extends ServiceConfigurable {
    public static final String PATH_AUTHSERVER = Strings.getService("authserver");
    private List<String> realms = new ArrayList<>();
    private byte[] clientSecret;
    private byte[] realmSecret;
    private int realmTimeout = 5000;

    public boolean isTrustedRealm(String name) {
        boolean result = false;

        for (Object object : realms) {
            if (object.equals(name))
                result = true;
        }

        return result;
    }

    public List<String> getRealms() {
        return realms;
    }

    protected void setRealms(List<String> realms) {
        this.realms = realms;
    }

    public byte[] getRealmSecret() {
        return realmSecret;
    }

    public AuthServerSettings setRealmSecret(byte[] secret) {
        this.realmSecret = secret;
        return this;
    }

    public byte[] getClientSecret() {
        return clientSecret;
    }

    public AuthServerSettings setClientSecret(byte[] secret) {
        this.clientSecret = secret;
        return this;
    }

    public int getRealmTimeout() {
        return realmTimeout;
    }

    public void setRealmTimeout(int realmTimeout) {
        this.realmTimeout = realmTimeout;
    }
}
