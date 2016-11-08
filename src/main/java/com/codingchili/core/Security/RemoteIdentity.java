package com.codingchili.core.Security;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

import com.codingchili.core.Configuration.Strings;

/**
 * @author Robin Duda
 */
public class RemoteIdentity implements Serializable {
    private String node;
    private String host;

    public RemoteIdentity() {}

    public RemoteIdentity(String node, String host) {
        this.node = node;
        this.host = host;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @JsonIgnore
    public String toString() {
        return node + Strings.LOG_AT + host;
    }
}