package com.codingchili.core.context;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.configuration.system.RemoteStorage;
import com.codingchili.core.configuration.system.StorageSettings;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.Validator;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.util.UUID;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * @author Robin Duda
 * <p>
 * context used by storage plugins.
 */
public class StorageContext<Value> extends SystemContext {
    private Logger logger = logger(getClass());
    private String identifier = "storage-" + UUID.randomUUID();
    private String database = "";
    private String collection = "";
    private Class<Value> aClass;
    private String plugin;

    public StorageContext() {
        super();
    }

    public StorageContext(CoreContext context) {
        super(context);
    }

    @Override
    public Logger logger(Class aClass) {
        return super.logger(aClass)
                .setMetadata(ID_COLLECTION, () -> collection)
                .setMetadata(ID_DB, () -> database);
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
     * @return the class to be stored in the storage.
     */
    public Class<Value> clazz() {
        return this.aClass;
    }

    /**
     * @return the plugin identifier to us as storage.
     */
    public String plugin() {
        return this.plugin;
    }

    /**
     * converts a json object into the generic template used by a storage
     *
     * @param json the json object to convert
     * @return json mapped to an object of generic type
     */
    @SuppressWarnings("unchecked")
    public Value toValue(JsonObject json) {
        return Serializer.unpack(json, aClass);
    }

    /**
     * converts a byte array to a value using the deserialization template class
     *
     * @param bytes bytes from a json-formatted string
     * @return generic Value inflated using the bytes and template class.
     */
    public Value toValue(byte[] bytes) {
        return Serializer.unpack(new String(bytes), aClass);
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
        return database;
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
        return Validator.plainText(comparable) && comparable.toString().length() >= minFeedbackChars();
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
        event(LOG_STORAGE_WATCHER)
                .put(ID_NAME, name)
                .put(ID_MESSAGE, CoreStrings.getWatcherFailed(cause)).send();
    }

    /**
     * Called when a value failed to expire as it was not found.
     *
     * @param name     the id of the object that was expired.
     * @param affected the number of items affected by the query.
     */
    public void onWatcherCompleted(String name, int affected) {
        event(LOG_STORAGE_WATCHER)
                .put(ID_NAME, name)
                .put(ID_COUNT, affected)
                .put(ID_MESSAGE, CoreStrings.WATCHER_COMPLETED).send();
    }

    public void onWatcherPaused(String name) {
        event(LOG_STORAGE_WATCHER)
                .put(ID_NAME, name)
                .put(ID_MESSAGE, CoreStrings.WATCHER_PAUSED).send();
    }

    public void onWatcherResumed(String name) {
        event(LOG_STORAGE_WATCHER)
                .put(ID_NAME, name)
                .put(ID_MESSAGE, CoreStrings.WATCHER_RESUMED).send();
    }

    /**
     * Called when the collection has been dropped/cleared.
     */
    public void onCollectionDropped() {
        event(LOG_STORAGE_CLEARED).send();
    }

    private void log(JsonObject json) {
        logger.log(json.put(LOG_STORAGE_DB, database).put(LOG_STORAGE_COLLECTION, collection));
    }

    /**
     * sets the class that is used for deserialization. This should be the same
     * or a supertype of the object type in the storage.
     *
     * @param aClass the class template to inflate
     * @return fluent
     */
    public StorageContext<Value> setClass(Class<Value> aClass) {
        this.aClass = aClass;
        this.logger = logger(aClass);
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
     * @param database name as string
     * @return fluent
     */
    public StorageContext<Value> setDatabase(String database) {
        this.database = database;
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
    @SuppressWarnings("unchecked")
    public StorageContext<Value> setPlugin(String plugin) {
        this.plugin = plugin;
        updateIdentifier();
        return this;
    }

    private void updateIdentifier() {
        this.logger = logger(getClass());
        this.identifier = CoreStrings.getDBIdentifier(database, collection, plugin);
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
