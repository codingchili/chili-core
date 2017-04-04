package com.codingchili.authentication.configuration;

import com.codingchili.common.Strings;
import com.codingchili.core.configuration.ServiceConfigurable;
import com.codingchili.core.storage.IndexedMap;

/**
 * Authentication service settings.
 */
public class AuthenticationSettings extends ServiceConfigurable {
    static final String PATH_AUTHSERVER = Strings.getService("authserver");
    private byte[] clientSecret;
    private String storage = IndexedMap.class.getName();

    /**
     * @return the configured client secret.
     */
    public byte[] getClientSecret() {
        return clientSecret;
    }

    /**
     * @param secret new client secret.
     * @return fluent
     */
    public AuthenticationSettings setClientSecret(byte[] secret) {
        this.clientSecret = secret;
        return this;
    }

    /**
     * @return the storage used to store credentials.
     */
    public String getStorage() {
        return storage;
    }

    /**
     * @param storage set the storage to use, identified as a string.
     */
    public void setStorage(String storage) {
        this.storage = storage;
    }
}
