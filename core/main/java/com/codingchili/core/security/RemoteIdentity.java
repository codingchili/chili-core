package com.codingchili.core.security;

import java.io.Serializable;

import static com.codingchili.core.configuration.CoreStrings.ID_UNDEFINED;

/**
 * @author Robin Duda
 *
 * Defines a remote identity as the identity/service name and the hostname.
 */
public class RemoteIdentity implements Serializable {
    private String node = ID_UNDEFINED;

    public RemoteIdentity() {}

    public RemoteIdentity(String node) {
        this.node = node;
    }

    public String getNode() {
        return node;
    }

    public RemoteIdentity setNode(String node) {
        this.node = node;
        return this;
    }
}
