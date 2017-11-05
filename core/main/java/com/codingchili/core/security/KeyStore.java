package com.codingchili.core.security;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.configuration.system.SecuritySettings;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;

/**
 * Used in #{@link SecuritySettings} to contain references to keystores.
 */
public class KeyStore {
    private String password = "password";
    private String path = CoreStrings.DEFAULT_KEYSTORE;
    private String shortName = CoreStrings.DEFAULT_KEYSTORE;

    /**
     * @return keystore password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password set keystore password. If unset reads the password from console.
     * @return fluent
     */
    public KeyStore setPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * @return path to the keystore.
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path path to the keystore.
     * @return fluent
     */
    public KeyStore setPath(String path) {
        this.path = path;
        if (shortName == null) {
            this.shortName = new File(path).getName();
        }
        return this;
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    /**
     * @param shortName sets the name the keystore is identified by.
     * @return fluent.
     */
    @JsonIgnore
    public KeyStore setShortName(String shortName) {
        this.shortName = shortName;
        return this;
    }

    /**
     * @return the name by which this keystore is identified.
     */
    @JsonIgnore
    public String getShortName() {
        return shortName;
    }
}
