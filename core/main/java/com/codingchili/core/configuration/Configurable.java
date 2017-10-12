package com.codingchili.core.configuration;

import com.codingchili.core.files.JsonFileStore;
import com.codingchili.core.protocol.Serializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vertx.core.json.JsonObject;

import java.io.Serializable;

/**
 * @author Robin Duda
 * <p>
 * Base configuration interface.
 */
public interface Configurable extends Serializable {
    /**
     * Get the path of a loaded configuration file.
     *
     * @return the directory path to the configuration file.
     */
    @JsonIgnore
    String getPath();

    /**
     * Set the path of a configurable to allow saving to the same location
     * it was loaded from.
     *
     * @param path the path to the configurable on disk for reloading and saving.
     */
    default void setPath(String path) {}

    /**
     * Serializes a configuration for permanent storage, allows the
     * configuration container to remove computed attributes that
     * are used when serializing over network but not to disk.
     *
     * @return a JsonObject that is equal to the JsonObject loaded from disk.
     */
    @JsonIgnore
    default JsonObject serialize() {
        return Serializer.json(this);
    }

    /**
     * Saves the configurable back to disk.
     */
    default void save() {
        JsonFileStore.writeObject(serialize(), getPath());
    }
}
