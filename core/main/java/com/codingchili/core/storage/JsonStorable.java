package com.codingchili.core.storage;

import io.vertx.core.json.JsonObject;

import java.util.*;

/**
 * Extends the JsonObject making it storable.
 */
public class JsonStorable extends JsonObject implements Storable {

    /**
     * Creates a new storable JsonObject by setting its object ID to an UUID.
     */
    public JsonStorable() {
        this.put(idField, UUID.randomUUID().toString());
    }

    /**
     * @param id the id of the storable to set, cannot be null.
     */
    public JsonStorable(String id) {
        Objects.requireNonNull(id, "The id of a JsonStorable must not be null.");
        this.put(idField, id);
    }

    /**
     * @param json creates a storable from the given json, if the given json does not contain
     *             an 'id' field one will be added using uuid.
     */
    public JsonStorable(JsonObject json) {
        super(json.getMap());
        if (!json.containsKey(idField)) {
            json.put(idField, UUID.randomUUID().toString());
        }
    }

    /**
     * @param map a key-value pairing representing a json structure.
     */
    public JsonStorable(Map<String, Object> map) {
        super(map);
    }

    /**
     * @return creates a copy of the backing json object and assigns a new id.
     */
    public JsonStorable copyNew() {
        var json = this.copy()
                .put(idField, UUID.randomUUID().toString());
        return new JsonStorable(json);
    }

    @Override
    public String getId() {
        return (this.containsKey(idField)) ?
                this.getString(Storable.idField) : this.hashCode() + "";
    }
}
