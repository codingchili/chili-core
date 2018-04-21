package com.codingchili.core.security;

import com.codingchili.core.files.Configurations;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;

/**
 * @author Robin Duda
 * <p>
 * Used to authenticate requests between services.
 */
public class Token implements Serializable {
    private Map<String, Object> properties = new HashMap<>();
    private String domain = UUID.randomUUID().toString();
    private String key = "";
    private long expiry = Instant.now().getEpochSecond() +
            Configurations.security().getTokenttl();

    public Token() {
    }

    public Token(TokenFactory factory, String domain) {
        this.domain = domain;
        factory.hmac(this);
    }

    public String getKey() {
        return key;
    }

    public Token setKey(String key) {
        this.key = key;
        return this;
    }

    public long getExpiry() {
        return expiry;
    }

    public Token setExpiry(long expiry) {
        this.expiry = expiry;
        return this;
    }

    public String getDomain() {
        return domain;
    }

    public Token setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    @JsonIgnore
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key) {
        return (T) properties.get(key);
    }

    public Token addProperty(String key, Object value) {
        properties.put(key, value);
        return this;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public Token setProperties(Map<String, Object> properties) {
        this.properties = properties;
        return this;
    }
}