package com.codingchili.core.Configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Robin Duda
 *         Used to write changes to configuration files.
 */
public interface Configurable {
    /**
     * Get the path of a loaded configuration file.
     *
     * @return the directory path to the configuration file.
     */
    @JsonIgnore
    String getPath();


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
