package com.codingchili.core.storage;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.StorageContext;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.Level;
import com.codingchili.core.logging.Logger;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * Builder to load storage plugins.
 */
public class StorageLoader<Value extends Storable> {
    private JsonObject properties;
    private Class<? extends AsyncStorage> plugin;
    private Class<Value> valueClass;
    private CoreContext context;
    private Logger logger;
    private String pluginString;
    private String database;
    private String collection;

    public StorageLoader() {
    }

    /**
     * Creates a new storage loader
     *
     * @param context the context to use.
     */
    public StorageLoader(CoreContext context) {
        this.context = context;
    }

    private Future<AsyncStorage<Value>> load() {
        var promise = Promise.<AsyncStorage<Value>>promise();
        try {
            prepare();

            StorageContext<Value> storage = new StorageContext<Value>(context)
                    .setDatabase(database)
                    .setCollection(collection)
                    .setClass(valueClass)
                    .setPlugin(plugin)
                    .setProperties(properties);

            var instance = plugin.getConstructor(Promise.class, StorageContext.class)
                    .<Value>newInstance(promise, storage);

        } catch (Throwable e) {
            logger.log(CoreStrings.getStorageLoaderError(pluginString, database, collection), Level.ERROR);
            logger.onError(e);
            promise.fail(e);
        }
        return promise.future();
    }

    @SuppressWarnings("unchecked")
    private void prepare() throws ClassNotFoundException {
        if (plugin == null) {
            this.plugin = (Class<? extends AsyncStorage>) Class.forName(pluginString);
        } else if (pluginString == null) {
            this.pluginString = plugin.getSimpleName();
        }

        if (collection == null) {
            this.collection = valueClass.getSimpleName();
        }

        if (database == null) {
            database = Configurations.storage().getSettingsForPlugin(plugin).getDatabase();
        }

        checkIsSet(context, ID_CONTEXT);
        checkIsSet(valueClass, ID_CLASS);
        checkIsSet(plugin, ID_PLUGIN);
    }

    /**
     * @param DB database name to use, if unset defaults to application name.
     * @return fluent.
     */
    public StorageLoader<Value> withDB(String DB) {
        this.database = DB;
        return this;
    }

    /**
     * @param DB         database name to use, if unset uses application name.
     * @param collection collection name to use, if unset uses storable class name.
     * @return fluent.
     */
    public StorageLoader<Value> withDB(String DB, String collection) {
        this.database = DB;
        this.collection = collection;
        return this;
    }

    /**
     * @param valueClass the class to be stored.
     * @return fluent.
     */
    public StorageLoader<Value> withValue(Class<Value> valueClass) {
        try {
            // trigger static init for value class.
            Class.forName(valueClass.getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        this.valueClass = valueClass;
        return this;
    }

    /**
     * @param properties implementation specific configuration options.
     * @return fluent.
     */
    public StorageLoader<Value> withProperties(JsonObject properties) {
        this.properties = properties;
        return this;
    }

    /**
     * @param plugin a plugin implementing #{@link AsyncStorage}.
     * @return fluent.
     */
    public StorageLoader<Value> withPlugin(Class<? extends AsyncStorage> plugin) {
        this.plugin = plugin;
        return this;
    }

    /**
     * @param collection collection name to use, defaults to storables class name.
     * @return fluent.
     */
    public StorageLoader<Value> withCollection(String collection) {
        this.collection = collection;
        return this;
    }

    /**
     * @param plugin a plugin to store the given class, must implement
     *               #{@link AsyncStorage}
     * @return fluent.
     */
    @SuppressWarnings("unchecked")
    public StorageLoader<Value> withPlugin(String plugin) {
        this.pluginString = plugin;
        return this;
    }

    /**
     * Loads the configured storage. Throws an exception if context,
     * class or plugin is unset.
     *
     * @return future completed when the storage is loaded.
     */
    public Future<AsyncStorage<Value>> build() {
        this.logger = context.logger(getClass());
        return this.load();
    }

    private void checkIsSet(Object object, String type) {
        Objects.requireNonNull(object, CoreStrings.getStorageLoaderMissingArgument(type));
    }
}
