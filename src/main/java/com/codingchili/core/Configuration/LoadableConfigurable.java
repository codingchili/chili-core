package com.codingchili.core.Configuration;

import com.codingchili.core.Protocols.Util.Serializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 *         Used to write changes to configuration files.
 */
public interface LoadableConfigurable {
    /**
     * Get the path of a loaded configuration file.
     *
     * @return the directory path to the configuration file.
     */
    @JsonIgnore
    String getPath();

    /**
     * Serializes a configuration for permanent storage, allows the
     * configuration container to remove computed attributes that
     * are used when serializing over network but not to disk.
     *
     * @return a JsonObject that is equal to the JsonObject loaded from disk.
     */
    default JsonObject serialize() {
        return Serializer.json(this);
    }
}
