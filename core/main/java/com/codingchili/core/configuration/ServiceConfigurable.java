package com.codingchili.core.configuration;


import com.fasterxml.jackson.annotation.JsonIgnore;

import com.codingchili.core.security.RemoteIdentity;

/**
 * @author Robin Duda
 *         <p>
 *         Service configurables are loaded by service contexts for use in services.
 */
public class ServiceConfigurable extends BaseConfigurable {
    private RemoteIdentity identity = new RemoteIdentity("unconfigured",
            Environment.hostname().orElse("undefined"));

    public ServiceConfigurable() {
    }

    public ServiceConfigurable(String path) {
        super(path);
    }

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

    public ServiceConfigurable setIdentity(RemoteIdentity identity) {
        this.identity = identity;
        return this;
    }
}
