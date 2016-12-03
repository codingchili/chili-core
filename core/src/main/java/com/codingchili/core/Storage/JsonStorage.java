package com.codingchili.core.storage;

import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 *
 * Interface for persisting json data, for example logging data.
 */
public interface JsonStorage {

    /**
     * Outputs a json object to a persistent storage.
     * @param json the json object to be persisted.
     */
    void output(JsonObject json);

}
