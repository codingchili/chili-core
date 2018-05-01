package com.codingchili.core.security;

import com.codingchili.core.files.Configurations;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;

/**
 * @author Robin Duda
 * <p>
 * Used to authenticate requests between services and with clients.
 */
public class Token implements Serializable {
    private Map<String, Object> properties = new HashMap<>();
    private String domain = UUID.randomUUID().toString();
    private String key = "";
    private long expiry = Instant.now().getEpochSecond() +
            Configurations.security().getTokenttl();

    /**
     * Creates a new empty unverified token.
     */
    public Token() {
    }

    /**
     * Creates a new hmac token with the specified token factory.
     *
     * @param factory the token factory to generate the hmac.
     * @param domain  a unique identifier - username or service id.
     */
    public Token(TokenFactory factory, String domain) {
        this.domain = domain;
        factory.hmac(this);
    }

    /**
     * @return the cipher value of the token.
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key the cipher value, this should be cryptographically generated and balidated.
     * @return fluent.
     */
    public Token setKey(String key) {
        this.key = key;
        return this;
    }

    /**
     * @return the time of expiry in epoch seconds.
     */
    public long getExpiry() {
        return expiry;
    }

    /**
     * @param expiry the time of expiry on epoch seconds.
     * @return fluent
     */
    public Token setExpiry(long expiry) {
        this.expiry = expiry;
        return this;
    }

    /**
     * @return the domain in which this token is valid for authentication.
     */
    public String getDomain() {
        return domain;
    }

    /**
     * @param domain the domain where this token should be valid for authentication.
     * @return fluent.
     */
    public Token setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    @JsonIgnore
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key) {
        return (T) properties.get(key);
    }

    /**
     * Adds a property to the token. This must be cryptographically incorporated in the {@link #getKey()}.
     *
     * @param key   the key of the property to set.
     * @param value the value of the property to set.
     * @return fluent.
     */
    public Token addProperty(String key, Object value) {
        properties.put(key, value);
        return this;
    }

    /**
     * @return all properties that are set on the token.
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * @param properties a map of properties to set.
     * @return fluent.
     */
    public Token setProperties(Map<String, Object> properties) {
        this.properties = properties;
        return this;
    }
}