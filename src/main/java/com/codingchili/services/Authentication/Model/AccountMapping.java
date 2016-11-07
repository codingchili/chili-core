package com.codingchili.services.Authentication.Model;

import java.io.Serializable;

/**
 * @author Robin Duda
 *         Database mapping not shared outside storage.
 */
class AccountMapping implements Serializable {
    private String username;
    private String email;
    private String salt;
    private String hash;

    public AccountMapping() {
    }

    public AccountMapping(Account account) {
        this.username = account.getUsername();
        this.email = account.getEmail();
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
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
}
