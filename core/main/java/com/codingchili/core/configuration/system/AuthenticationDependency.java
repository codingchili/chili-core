package com.codingchili.core.configuration.system;

import java.util.HashMap;
import java.util.HashSet;

import com.codingchili.core.security.TokenIdentifier;

/**
 * Defines preshared secrets between services, local secrets and tokens
 * which are generated from another services secret.
 */
public class AuthenticationDependency {
    private HashSet<String> preshare = new HashSet<>();
    private HashSet<String> secrets = new HashSet<>();
    private HashMap<String, TokenIdentifier> tokens = new HashMap<>();
    private HashSet<String> keypairs = new HashSet<>();
    private HashSet<String> publickeys = new HashSet<>();

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

    /**
     * @return the key pairs that the matching files sign with: they get the private and the public key
     * (written as {@code <name>PrivateKey} and {@code <name>PublicKey}).
     */
    public HashSet<String> getKeypairs() {
        return keypairs;
    }

    public void setKeypairs(HashSet<String> keypairs) {
        this.keypairs = keypairs;
    }

    /**
     * @return the key pairs that the matching files verify with: they get only the public key
     * ({@code <name>PublicKey}) and cannot sign.
     */
    public HashSet<String> getPublickeys() {
        return publickeys;
    }

    public void setPublickeys(HashSet<String> publickeys) {
        this.publickeys = publickeys;
    }

    public AuthenticationDependency addKeypair(String name) {
        keypairs.add(name);
        return this;
    }

    public AuthenticationDependency addPublickey(String name) {
        publickeys.add(name);
        return this;
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
