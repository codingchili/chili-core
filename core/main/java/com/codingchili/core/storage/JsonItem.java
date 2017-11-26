package com.codingchili.core.storage;

import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 * <p>
 * Extends the JsonObject making it storable.
 */
public class JsonItem extends JsonObject implements Storable {

    public JsonItem() {
    }

    public JsonItem(JsonObject json) {

    }

    @Override
    public String id() {
        return (this.containsKey(idField)) ?
                this.getString(Storable.idField) : this.hashCode() + "";
    }
}
