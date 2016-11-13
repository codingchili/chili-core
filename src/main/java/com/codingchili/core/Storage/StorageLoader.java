package com.codingchili.core.Storage;

import io.vertx.core.Future;

import com.codingchili.core.Configuration.Strings;
import com.codingchili.core.Configuration.System.LauncherSettings;
import com.codingchili.core.Context.CoreContext;
import com.codingchili.core.Context.StorageContext;
import com.codingchili.core.Logging.Level;

import com.codingchili.services.Realm.Configuration.RealmSettings;

/**
 * @author Robin Duda
 *         <p>
 *         Loads the given storage subsystem.
 */
public class StorageLoader {
    private static CoreContext context;

    public static void initialize(CoreContext context) {
        StorageLoader.context = context;
    }

    public static <Key, Value> void load(Future<AsyncStorage<Key, Value>> future, String className, String mapName) {
        try {
            StorageContext<Value> storage = new StorageContext<Value>(context)
                    .setDB(mapName)
                    .setClass(LauncherSettings.class);

            Class.forName(className)
                    .getConstructor(Future.class, StorageContext.class).<Key, Value>newInstance(future, storage);
        } catch (ReflectiveOperationException e) {
            context.console().log(Strings.getStorageLoaderError(className, mapName), Level.SEVERE);
            System.exit(0);
        }
    }
}
