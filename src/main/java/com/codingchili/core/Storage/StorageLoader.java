package com.codingchili.core.Storage;

import io.vertx.core.Future;

import com.codingchili.core.Configuration.Strings;
import com.codingchili.core.Context.CoreContext;
import com.codingchili.core.Context.StorageContext;
import com.codingchili.core.Logging.Level;

import static com.codingchili.services.Shared.Strings.*;

/**
 * @author Robin Duda
 *         <p>
 *         Loads the given storage subsystem.
 */
public class StorageLoader {
    private CoreContext context;
    private String DB;
    private Class clazz;
    private String plugin;

    private StorageLoader() {
    }

    private <Key, Value> void load(Future<AsyncStorage<Key, Value>> future) {
        try {
            StorageContext<Value> storage = new StorageContext<Value>(context)
                    .setDB(DB)
                    .setClass(clazz);

            Class.forName(plugin)
                    .getConstructor(Future.class, StorageContext.class)
                    .<Key, Value>newInstance(future, storage);

        } catch (ReflectiveOperationException e) {
            context.console().log(Strings.getStorageLoaderError(plugin, DB), Level.SEVERE);
            System.exit(0);
        }
    }

    public static StorageLoader prepare() {
        return new StorageLoader();
    }

    public StorageLoader withContext(CoreContext context) {
        this.context = context;
        return this;
    }

    public StorageLoader withDB(String DB) {
        this.DB = DB;
        return this;
    }

    public StorageLoader withClass(Class clazz) {
        this.clazz = clazz;
        return this;
    }

    public StorageLoader withPlugin(Class plugin) {
        this.plugin = plugin.getCanonicalName();
        return this;
    }

    public StorageLoader withPlugin(String plugin) {
        this.plugin = plugin;
        return this;
    }

    public <Key, Value> void build(Future<AsyncStorage<Key, Value>> future) {
        checkIsSet(context, ID_CONTEXT);
        checkIsSet(DB, ID_DB);
        checkIsSet(clazz, ID_CLASS);
        checkIsSet(plugin, ID_PLUGIN);

        this.load(future);
    }

    private void checkIsSet(Object object, String type) {
        if (object == null) {
            throw new RuntimeException(Strings.getStorageLoaderMissingArgument(type));
        }
    }
}
