package com.codingchili.core.security;

import java.io.File;

import com.codingchili.core.configuration.system.SecuritySettings;

/**
 * Used in #{@link SecuritySettings} to contain references to keystores.
 */
public class KeyStore {
    private String password = "changeit";
    private String path;
    private String shortName;

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

    @Override
    public boolean equals(Object other) {
        return ((KeyStore) other).getPath().equals(path);
    }

    /**
     * @param shortName sets the name the keystore is identified by.
     * @return fluent.
     */
    public KeyStore setShortName(String shortName) {
        this.shortName = shortName;
        return this;
    }

    /**
     * @return the name by which this keystore is identified.
     */
    public String getShortName() {
        return shortName;
    }
}
