package com.codingchili.realmregistry.configuration;

import com.codingchili.common.Strings;
import com.codingchili.core.configuration.ServiceConfigurable;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

/**
 * Settings for the realm registry.
 */
public class RealmRegistrySettings extends ServiceConfigurable {
    static final String PATH_REALMREGISTRY = Strings.getService("realmregistry");
    private List<String> realms = new ArrayList<>();
    private byte[] realmSecret = new byte[]{0x54};
    private byte[] clientSecret = new byte[]{0x48};
    private int realmTimeout = 5000;

    /**
     * @param name the handler of the realm to check if trusted.
     * @return true if the realm is not a third-party realm.
     */
    public boolean isTrustedRealm(String name) {
        boolean result = false;

        for (Object object : realms) {
            if (object.equals(name))
                result = true;
        }

        return result;
    }

    /**
     * @return get a list of realms as strings.
     */
    public List<String> getRealms() {
        return realms;
    }

    /**
     * @param realms set a list of reals as strings.
     */
    protected void setRealms(List<String> realms) {
        this.realms = realms;
    }

    /**
     * @param realm handler of the realm to add.
     */
    @JsonIgnore
    protected void addRealm(String realm) {
        this.realms.add(realm);
    }

    /**
     * @return get the realm tokens secret key.
     */
    public byte[] getRealmSecret() {
        return realmSecret;
    }

    /**
     * @param secret sets the realm tokens secret key.
     * @return fluent
     */
    public RealmRegistrySettings setRealmSecret(byte[] secret) {
        this.realmSecret = secret;
        return this;
    }

    /**
     * @return get the time in MS which a realm is removed after if no updates are received.
     */
    public int getRealmTimeout() {
        return realmTimeout;
    }

    /**
     * @param realmTimeout set the time in MS which a realm is removed after no updates are received.
     */
    public void setRealmTimeout(int realmTimeout) {
        this.realmTimeout = realmTimeout;
    }

    /**
     * @return get the client tokens secret key.
     */
    public byte[] getClientSecret() {
        return clientSecret;
    }

    /**
     * @param clientSecret set the client tokens secret key.
     */
    public void setClientSecret(byte[] clientSecret) {
        this.clientSecret = clientSecret;
    }
}
