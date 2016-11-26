package com.codingchili.core.context;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.protocol.Serializer;

/**
 * @author Robin Duda
 *         <p>
 *         context used for storage plugins.
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

    /**
     * sets the class that is used for deserialization. This should be the same
     * or a supertype of the object type in the storage.
     *
     * @param clazz the class template to inflate
     */
    public StorageContext<Value> setClass(Class clazz) {
        this.clazz = clazz;
        return this;
    }

    /**
     * sets the collection context of the storage engine.
     *
     * @param collection name of the collection
     */
    public StorageContext<Value> setCollection(String collection) {
        this.collection = collection;
        return this;
    }

    /**
     * sets the database name to be used
     *
     * @param DB name as string
     */
    public StorageContext<Value> setDB(String DB) {
        this.DB = DB;
        return this;
    }

    /**
     * converts a json object into the generic template used by a storage
     *
     * @param json the json object to convert
     * @return json mapped to an object of generic type
     */
    @SuppressWarnings("unchecked")
    public Value toValue(JsonObject json) {
        return Serializer.unpack(json, clazz);
    }

    /**
     * converts a byte array to a value using the deserialization template class
     *
     * @param bytes bytes from a json-formatted string
     * @return generic Value inflated using the bytes and template class.
     */
    public Value toValue(byte[] bytes) {
        return Serializer.unpack(new String(bytes), clazz);
    }

    /**
     * converts the given object of generic type into a json object.
     *
     * @param object the object to be converted to json
     * @return the serialized form of the given object
     */
    public JsonObject toJson(Value object) {
        return Serializer.json(object);
    }

    /**
     * converts the given object of generic type into a byte array
     *
     * @param value the object to be converted
     * @return a byte array created from the serialized objects json text
     */
    public byte[] toPacked(Value value) {
        return Serializer.pack(value).getBytes();
    }

    /**
     * handles a handler successfully with the given value.
     * @param handler the handler to be handled
     * @param value the value to send the handler.
     */
    public void handle(Handler<AsyncResult<Value>> handler, Value value) {
        handler.handle(Future.succeededFuture(value));
    }

    public String DB() {
        return DB;
    }

    public String collection() {
        return collection;
    }
}
