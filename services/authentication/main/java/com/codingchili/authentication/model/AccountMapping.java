package com.codingchili.authentication.model;


import com.codingchili.core.security.Account;
import com.codingchili.core.storage.Storable;

/**
 * @author Robin Duda
 * Database mapping not shared outside storage.
 */
public class AccountMapping implements Storable {
    private String username;
    private String email;
    private String hash;

    public AccountMapping() {
    }

    public AccountMapping(Account account) {
        this.username = account.getUsername();
        this.email = account.getEmail();
    }

    public String getUsername() {
        return username;
    }

    public AccountMapping setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getEmail() {
        return email;
    }

    public AccountMapping setEmail(String email) {
        this.email = email;
        return this;
    }

    @Override
    public String getId() {
        return username;
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return compareTo(other) == 0;
    }
}
