package com.codingchili.core.context;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.protocol.Serializer;

import static com.codingchili.core.configuration.Strings.*;

/**
 * @author Robin Duda
 *         <p>
 *         context used by storage plugins.
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
     *
     * @param handler the handler to be handled
     * @param value   the value to send the handler.
     */
    public void handle(Handler<AsyncResult<Value>> handler, Value value) {
        handler.handle(Future.succeededFuture(value));
    }

    /**
     * get the name of the database used by the context.
     * @return the name of the database as a string
     */
    public String DB() {
        return DB;
    }

    /**
     * get the name of the collection within the database used by the context.
     * @return the name of the collection as a string.
     */
    public String collection() {
        return collection;
    }

    /**
     * Called when a value has been expired by ttl.
     * @param key the id of the object that was expired.
     * @param ttl the time at which the object was set to expire.
     */
    public void onValueExpired(String key, Long ttl) {
        log(event(LOG_VALUE_EXPIRED)
                .put(ID_KEY, key)
                .put(ID_TIME, timestamp(ttl)));
    }

    /**
     * Called when a value failed to expire as it was not found.
     * @param key the id of the object that was expired.
     * @param ttl the time at which the object was set to expire.
     */
    public void onValueExpiredMissing(String key, Long ttl) {
        log(event(LOG_VALUE_EXPIRED_MISSING)
                .put(ID_KEY, key)
                .put(ID_TIME, timestamp(ttl)));
    }

    /**
     * Called when the collection has been dropped/cleared.
     */
    public void onCollectionDropped() {
        log(event(LOG_STORAGE_CLEARED));
    }

    private void log(JsonObject json) {
        console().log(json.put(STORAGE_DATABASE, DB).put(STORAGE_COLLECTION, collection));
    }
}
