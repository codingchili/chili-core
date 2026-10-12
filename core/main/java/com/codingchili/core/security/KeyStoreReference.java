package com.codingchili.core.security;

import com.codingchili.core.configuration.system.SecuritySettings;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.KeyStoreOptionsBase;

import java.io.File;
import java.nio.file.Path;

import static com.codingchili.core.configuration.CoreStrings.DEFAULT_KEYSTORE;

/**
 * Used in #{@link SecuritySettings} to contain references to keystores.
 */
public class KeyStoreReference extends KeyStoreOptionsBase {
    public static final String TYPE_JKS = "JKS";
    public static final String TYPE_PKCS12 = "PKCS12";
    private String alias;
    private boolean auto = false;

    public KeyStoreReference() {
        setType(TYPE_JKS);
    }

    /**
     * @param alias sets the name the keystore is identified by.
     * @return fluent.
     */
    public KeyStoreReference setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    /**
     * @return the name by which this keystore is identified.
     */
    public String getAlias() {
        return alias;
    }

    @JsonIgnore
    public String getIdentifier() {
        if (alias != null) {
            return alias;
        } else {
            if (auto) {
                return DEFAULT_KEYSTORE;
            } else {
                return Path.of(getPath()).getFileName().toString();
            }
        }
    }

    /**
     * @return if a self-signed certificate should be generated on demand.
     */
    public boolean isAuto() {
        return auto;
    }

    /**
     * @param auto if a self-signed certificate should be generated on demand.
     * @return fluent
     */
    public KeyStoreReference setAuto(boolean auto) {
        this.auto = auto;
        return this;
    }

    /**
     * @param path path to the keystore.
     * @return fluent
     */
    public KeyStoreReference setPath(String path) {
        super.setPath(path);
        if (alias == null) {
            this.alias = new File(path).getName();
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
        return String.format("%s/%s", getPath(), getIdentifier()).hashCode();
    }

    private KeyStoreReference(KeyStoreReference copy) {
        this.setPath(copy.getPath());
        this.setPassword(copy.getPassword());
        this.setType(copy.getType());
        this.setAlias(copy.getAlias());
        this.setValue(copy.getValue());
        this.setProvider(copy.getProvider());
    }

    @Override
    public KeyStoreOptionsBase clone() {
        return new KeyStoreReference(this);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof KeyStoreReference) {
            return ((KeyStoreReference) other).getIdentifier().equals(getIdentifier());
        } else {
            return false;
        }
    }
}
