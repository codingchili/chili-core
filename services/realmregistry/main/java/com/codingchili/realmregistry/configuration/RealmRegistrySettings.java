package com.codingchili.realmregistry.configuration;

import com.codingchili.common.Strings;

import java.util.ArrayList;
import java.util.List;

import com.codingchili.core.configuration.ServiceConfigurable;


public class RealmRegistrySettings extends ServiceConfigurable {
    public static final String PATH_REALMREGISTRY = Strings.getService("realmregistry");
    private List<String> realms = new ArrayList<>();
    private byte[] realmSecret = new byte[] {0x54};
    private byte[] clientSecret = new byte[] {0x48};
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
