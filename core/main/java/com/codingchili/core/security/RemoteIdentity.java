package com.codingchili.core.security;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.configuration.Environment;

/**
 * @author Robin Duda
 *
 * Defines a remote identity as the node/service name and the hostname.
 */
public class RemoteIdentity implements Serializable {
    private String node = "unconfigured";
    private String host = Environment.hostname().orElse("undefined");

    public RemoteIdentity() {}

    public RemoteIdentity(String node, String host) {
        this.node = node;
        this.host = host;
    }

    public String getNode() {
        return node;
    }

    public RemoteIdentity setNode(String node) {
        this.node = node;
        return this;
    }

    public String getHost() {
        return host;
    }

    public RemoteIdentity setHost(String host) {
        this.host = host;
        return this;
    }

    @JsonIgnore
    public String toString() {
        return node + CoreStrings.LOG_AT + host;
    }
}
