package com.codingchili.realmregistry.configuration;

import java.util.ArrayList;
import java.util.List;

import com.codingchili.core.configuration.ServiceConfigurable;
import com.codingchili.common.Strings;


public class RealmRegistrySettings extends ServiceConfigurable {
    public static final String PATH_REALMREGISTRY = Strings.getService("realmregistry");
    private List<String> realms = new ArrayList<>();
    private byte[] realmSecret;
    private byte[] clientSecret;
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

    public RealmRegistrySettings setRealmSecret(byte[] secret) {
        this.realmSecret = secret;
        return this;
    }

    public int getRealmTimeout() {
        return realmTimeout;
    }

    public void setRealmTimeout(int realmTimeout) {
        this.realmTimeout = realmTimeout;
    }

    public byte[] getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(byte[] clientSecret) {
        this.clientSecret = clientSecret;
    }
}
