package com.codingchili.core.storage;

import io.vertx.core.json.JsonObject;

/**
 * Extends the JsonObject making it storable.
 */
public class JsonItem extends JsonObject implements Storable {

    public JsonItem() {
    }

    public JsonItem(JsonObject json) {

    }

    @Override
    public String getId() {
        return (this.containsKey(idField)) ?
                this.getString(Storable.idField) : this.hashCode() + "";
    }
}
