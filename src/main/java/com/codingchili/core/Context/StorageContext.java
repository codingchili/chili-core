package com.codingchili.core.Context;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.Protocol.Serializer;

/**
 * @author Robin Duda
 *
 * Context used for storage plugins.
 */
public class StorageContext<Value> extends SystemContext {
    private String DB;
    private String collection;
    private Class clazz;

    public StorageContext(CoreContext context) {
        super(context);
    }

    public StorageContext(Vertx vertx) {
        super(vertx);
    }

    public StorageContext<Value> setClass(Class clazz) {
        this.clazz = clazz;
        return this;
    }

    public StorageContext<Value>  setCollection(String collection) {
        this.collection = collection;
        return this;
    }

    public StorageContext<Value> setDB(String DB) {
        this.DB = DB;
        return this;
    }

    public Value toValue(JsonObject json) {
        return Serializer.unpack(json, clazz);
    }

    public JsonObject toJson(Value object) {
        return Serializer.json(object);
    }

    public AsyncResult<Value> convertJson(AsyncResult<JsonObject> json) {
        if (json.succeeded()) {
            return Future.succeededFuture(toValue(json.result()));
        } else {
            return Future.failedFuture(json.cause());
        }
    }

    public AsyncResult<Void> convertVoid(AsyncResult<?> result) {
        if (result.succeeded()) {
            return Future.succeededFuture();
        } else {
            return Future.failedFuture(result.cause());
        }
    }

    public String DB() {
        return DB;
    }

    public String collection() {
        return collection;
    }
}
