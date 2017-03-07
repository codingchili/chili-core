package com.codingchili.core.security;

import com.codingchili.core.storage.Storable;

public class Account implements Storable {
    private String username;
    private String password;
    private String email;

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

    public String getPassword() {
        return password;
    }

    public Account setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public Account setEmail(String email) {
        this.email = email;
        return this;
    }
}
