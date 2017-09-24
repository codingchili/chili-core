package com.codingchili.core.security;

import com.codingchili.core.storage.Storable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Robin Duda
 * <p>
 * Transfer object used for authentication from clients.
 * Password is to be consumed once read.
 */
public class Account implements Storable {
    private Map<String, Object> properties = new HashMap<>();
    private String username = "";
    private String password = "";
    private String email = "";

    public Account() {
    }

    public Account(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Account(Account account) {
        this.username = account.getUsername();
        this.email = account.getEmail();
    }

    public String getUsername() {
        return username;
    }

    public Account setUsername(String username) {
        this.username = username;
        return this;
    }

    /**
     * prefer using getCharPassword over this, as this method creates a new
     * copy in memory of the password. It is required for serialization purposes.
     *
     * @return a copy of the raw char array as a string.
     */
    public String getPassword() {
        return password;
    }

    public Account setPassword(String password) {
        this.password = password;
        return this;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public Account setProperties(Map<String, Object> properties) {
        this.properties = properties;
        return this;
    }

    public Account addProperty(String key, Object value) {
        properties.put(key, value);
        return this;
    }

    public String getEmail() {
        return email;
    }

    public Account setEmail(String email) {
        this.email = email;
        return this;
    }

    @Override
    public String id() {
        return username;
    }

    @Override
    public int hashCode() {
        return id().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return compareTo(other) == 0;
    }
}
