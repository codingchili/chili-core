package com.codingchili.core.security;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.configuration.Environment;

import static com.codingchili.core.configuration.CoreStrings.ID_DEFAULT;
import static com.codingchili.core.configuration.CoreStrings.ID_UNDEFINED;

/**
 * @author Robin Duda
 *
 * Defines a remote identity as the node/service name and the hostname.
 */
public class RemoteIdentity implements Serializable {
    private String node = ID_UNDEFINED;
    private String version = ID_UNDEFINED;
    private String host = Environment.hostname().orElse(ID_UNDEFINED);

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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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
