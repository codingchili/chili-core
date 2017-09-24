package com.codingchili.core.configuration.system;

import com.codingchili.core.security.TokenIdentifier;

import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Robin Duda
 * <p>
 * Defines preshared secrets between services, local secrets and tokens
 * which are generated from another services secret.
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

    public AuthenticationDependency addPreshare(String name) {
        preshare.add(name);
        return this;
    }

    public AuthenticationDependency addSecret(String name) {
        secrets.add(name);
        return this;
    }

    public AuthenticationDependency addToken(String name, String service, String secret) {
        tokens.put(name, new TokenIdentifier(service, secret));
        return this;
    }
}
