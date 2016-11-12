package com.codingchili.core.Configuration;


import com.fasterxml.jackson.annotation.JsonIgnore;

import com.codingchili.core.Security.RemoteIdentity;

/**
 * @author Robin Duda
 *
 * Service configurables are loaded by service contexts for use in services.
 */
public class ServiceConfigurable extends WritableConfigurable {
    private RemoteIdentity identity = new RemoteIdentity("unconfigured", "unconfigured");

    @JsonIgnore
    public String host() {
        return identity.getHost();
    }

    @JsonIgnore
    public String node() {
        return identity.getNode();
    }

    public RemoteIdentity getIdentity() {
        return identity;
    }

    public void setIdentity(RemoteIdentity identity) {
        this.identity = identity;
    }
}
