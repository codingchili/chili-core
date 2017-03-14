package com.codingchili.core.security;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.codingchili.core.storage.Storable;

/**
 * @author Robin Duda
 *
 * Transfer object used for authentication from clients.
 * Password is to be consumed once read.
 */
public class Account implements Storable {
    private String username = "";
    private char[] password = "".toCharArray();
    private String email = "";

    public Account() {
    }

    public Account(String username, String password) {
        this.username = username;
        this.password = password.toCharArray();
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

    public String getPassword() {
        return new String(password);
    }

    public Account setPassword(String password) {
        this.password = password.toCharArray();
        return this;
    }

    public String getEmail() {
        return email;
    }

    public Account setEmail(String email) {
        this.email = email;
        return this;
    }

    @JsonIgnore
    public char[] getCharPassword() {
        return password;
    }
}
