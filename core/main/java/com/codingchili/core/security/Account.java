package com.codingchili.core.security;

import java.util.ArrayList;
import java.util.List;

import com.codingchili.core.storage.Storable;

/**
 * @author Robin Duda
 *         <p>
 *         Transfer object used for authentication from clients.
 *         Password is to be consumed once read.
 */
public class Account implements Storable {
    private List<String> servers = new ArrayList<>();
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

    public List<String> getServers() {
        return servers;
    }

    public void setServers(List<String> servers) {
        this.servers = servers;
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
    public boolean equals(Object other) {
        return compareTo(other) == 0;
    }
}
