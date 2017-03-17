package com.codingchili.core.context;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;

import java.util.UUID;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.configuration.system.RemoteStorage;
import com.codingchili.core.configuration.system.StorageSettings;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.Validator;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * @author Robin Duda
 *         <p>
 *         context used by storage plugins.
 */
public class StorageContext<Value> extends SystemContext {
    private Validator validator = new Validator();
    private String identifier = "storage-" + UUID.randomUUID();
    private String DB = "";
    private String collection = "";
    private Class clazz;
    private String plugin;

    public StorageContext(CoreContext context) {
        super(context);
    }

    public StorageContext(Vertx vertx) {
        super(vertx);
    }

    /**
     * @return get the storage settings.
     */
    protected StorageSettings settings() {
        return Configurations.storage();
    }

    /**
     * @return get the storage settings for the current plugin.
     */
    public RemoteStorage storage() {
        return settings().storage(plugin);
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
     *
     * @return the name of the database as a string
     */
    public String DB() {
        return DB;
    }

    /**
     * get the name of the collection within the database used by the context.
     *
     * @return the name of the collection as a string.
     */
    public String collection() {
        return collection;
    }

    /**
     * @return the port of a remote database if configured.
     */
    public Integer port() {
        return settings().getStorage().get(plugin).getPort();
    }

    /**
     * @return the hostname of a remote database if configured.
     */
    public String host() {
        return settings().getStorage().get(plugin).getHost();
    }

    public String dbPath() {
        return CoreStrings.getDBPath(identifier());
    }

    /**
     * Validates that the given string consists only of plaintext and is at least as
     * long as the specified number of feedback-characters.
     *
     * @param comparable the text to check if plaintext
     * @return true if the given comparable contains only plaintext
     */
    public boolean validate(Comparable comparable) {
        return validator.plainText(comparable) && comparable.toString().length() >= minFeedbackChars();
    }

    public Integer minFeedbackChars() {
        return settings().getMinFeedbackChars();
    }

    /**
     * @return the number of results all queries are limited to.
     */
    public Integer maxResults() {
        return settings().getMaxResults();
    }

    /**
     * Called when a value has been expired by ttl.
     *
     * @param name  the id of the object that was expired.
     * @param cause the reason why invocation failed.
     */
    public void onWatcherFailed(String name, String cause) {
        log(event(LOG_STORAGE_WATCHER)
                .put(ID_NAME, name)
                .put(ID_MESSAGE, CoreStrings.getWatcherFailed(cause)));
    }

    /**
     * Called when a value failed to expire as it was not found.
     *
     * @param name     the id of the object that was expired.
     * @param affected the number of items affected by the query.
     */
    public void onWatcherCompleted(String name, int affected) {
        log(event(LOG_STORAGE_WATCHER)
                .put(ID_NAME, name)
                .put(ID_COUNT, affected)
                .put(ID_MESSAGE, CoreStrings.WATCHER_COMPLETED));
    }

    public void onWatcherPaused(String name) {
        log(event(LOG_STORAGE_WATCHER)
            .put(ID_NAME, name)
            .put(ID_MESSAGE, CoreStrings.WATCHER_PAUSED));
    }

    public void onWatcherResumed(String name) {
        log(event(LOG_STORAGE_WATCHER)
                .put(ID_NAME, name)
                .put(ID_MESSAGE, CoreStrings.WATCHER_RESUMED));
    }

    /**
     * Called when the collection has been dropped/cleared.
     */
    public void onCollectionDropped() {
        log(event(LOG_STORAGE_CLEARED));
    }

    private void log(JsonObject json) {
        console().log(json.put(LOG_STORAGE_DB, DB).put(LOG_STORAGE_COLLECTION, collection));
    }

    /**
     * sets the class that is used for deserialization. This should be the same
     * or a supertype of the object type in the storage.
     *
     * @param clazz the class template to inflate
     * @return fluent
     */
    public StorageContext<Value> setClass(Class clazz) {
        this.clazz = clazz;
        return this;
    }

    /**
     * sets the collection context of the storage engine.
     *
     * @param collection name of the collection
     * @return fluent
     */
    public StorageContext<Value> setCollection(String collection) {
        this.collection = collection;
        updateIdentifier();
        return this;
    }

    /**
     * sets the database name to be used
     *
     * @param DB name as string
     * @return fluent
     */
    public StorageContext<Value> setDB(String DB) {
        this.DB = DB;
        updateIdentifier();
        return this;
    }

    /**
     * sets the storage plugin name the context is bound to so that configuration for
     * it may be retrieved.
     *
     * @param plugin fully qualified class name as String.
     * @return fluent
     */
    public StorageContext<Value> setPlugin(String plugin) {
        this.plugin = plugin;
        updateIdentifier();
        return this;
    }

    private void updateIdentifier() {
        if (plugin != null) {
            this.identifier = CoreStrings.getDBIdentifier(DB, collection);
        }
    }

    /**
     * Creates an identifier based on the plugin class name, the database name
     * and the collection that this context is operating on.
     *
     * @return a somewhat unique string for this context.
     */
    public String identifier() {
        return identifier;
    }
}
