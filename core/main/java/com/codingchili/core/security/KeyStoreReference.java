package com.codingchili.core.security;

import com.codingchili.core.configuration.system.SecuritySettings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.KeyStoreOptionsBase;

import java.io.File;

/**
 * Used in #{@link SecuritySettings} to contain references to keystores.
 */
public class KeyStoreReference extends KeyStoreOptionsBase {
    public static final String TYPE_JKS = "JKS";
    public static final String TYPE_PKCS12 = "PKCS12";
    private String shortName;

    public KeyStoreReference() {
        setType(TYPE_JKS);
    }

    /**
     * @param shortName sets the name the keystore is identified by.
     * @return fluent.
     */
    public KeyStoreReference setShortName(String shortName) {
        this.shortName = shortName;
        return this;
    }

    /**
     * @return the name by which this keystore is identified.
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * @param path path to the keystore.
     * @return fluent
     */
    public KeyStoreReference setPath(String path) {
        super.setPath(path);
        if (shortName == null) {
            this.shortName = new File(path).getName();
        }
        return this;
    }

    // modify visibility for existing methods.
    @Override
    public KeyStoreOptionsBase setType(String type) {
        return super.setType(type);
    }

    @Override
    public String getType() {
        return super.getType();
    }

    @Override
    protected String getProvider() {
        return super.getProvider();
    }

    @JsonIgnore
    @Override
    public Buffer getValue() {
        return super.getValue();
    }

    @Override
    public KeyStoreOptionsBase copy() {
        return this;
    }

    @Override
    protected KeyStoreOptionsBase setProvider(String provider) {
        return super.setProvider(provider);
    }

    @Override
    public int hashCode() {
        return getPath().hashCode();
    }

    private KeyStoreReference(KeyStoreReference copy) {
        this.setPath(copy.getPath());
        this.setPassword(copy.getPassword());
        this.setType(copy.getType());
        this.setShortName(copy.getShortName());
        this.setValue(copy.getValue());
        this.setProvider(copy.getProvider());
    }

    @Override
    public KeyStoreOptionsBase clone() {
        return new KeyStoreReference(this);
    }

    @Override
    public boolean equals(Object other) {
        return ((KeyStoreReference) other).getPath().equals(getPath());
    }
}
