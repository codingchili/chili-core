package com.codingchili.core.Configuration.System;

import java.util.HashMap;
import java.util.HashSet;

import com.codingchili.core.Security.TokenIdentifier;

/**
 * @author Robin Duda
 */
public class AuthenticationDependency {
    private HashSet<String> preshare = new HashSet<>();
    private HashSet<String> secrets = new HashSet<>();
    private HashMap<String, TokenIdentifier> tokens = new HashMap<>();

    public HashSet<String> getPreshare() {
        return preshare;
    }

    public void setPreshare(HashSet<String> preshare) {
        this.preshare = preshare;
    }

    public HashSet<String> getSecrets() {
        return secrets;
    }

    public void setSecrets(HashSet<String> secrets) {
        this.secrets = secrets;
    }

    public HashMap<String, TokenIdentifier> getTokens() {
        return tokens;
    }

    public void setTokens(HashMap<String, TokenIdentifier> tokens) {
        this.tokens = tokens;
    }
}
