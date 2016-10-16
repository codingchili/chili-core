package com.codingchili.core.Configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Robin Duda
 */
public interface Configurable extends LoadableConfigurable {

    /**
     * Get the name of the service within the configurable.
     *
     * @return the name of the service.
     */
    @JsonIgnore
    String getName();

    /**
     * Get the logserver configuration for the configurable.
     *
     * @return configuration of a logging server.
     */
    RemoteAuthentication getLogserver();

}
