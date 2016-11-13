package com.codingchili.core.Storage;

import io.vertx.core.*;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.unit.TestContext;
import org.junit.*;
import org.junit.runner.RunWith;


import com.codingchili.core.Context.CoreContext;
import com.codingchili.core.Context.SystemContext;

import com.codingchili.services.Shared.Strings;

import static com.codingchili.core.Configuration.Strings.STORAGE_HAZELMAP;
import static com.codingchili.core.Configuration.Strings.STORAGE_LOCALMAP;

/**
 * @author Robin Duda
 */
@RunWith(VertxUnitRunner.class)
public class StorageLoaderIT {
    private static CoreContext context;

    @BeforeClass
    public static void setUp(TestContext test) {
        Async async = test.async();

        Vertx.clusteredVertx(new VertxOptions(), vertx -> {
            context = new SystemContext(vertx.result());
            StorageLoader.initialize(context);
            async.complete();
        });
    }

    @Test
    public void testLoadLocalAsyncMap(TestContext test) {
        Async async = test.async();
        Future<AsyncStorage<String, String>> future = Future.future();

        future.setHandler(storage -> storage.result().put("KEY", "VALUE", result -> {
            test.assertTrue(result.succeeded());
            async.complete();
        }));

        StorageLoader.load(future, STORAGE_LOCALMAP, Strings.MAP_ACCOUNTS);
    }

    @Test
    public void testLoadHazelAsyncMap(TestContext test) {
        Async async = test.async();
        Future<AsyncStorage<String, String>> future = Future.future();

        future.setHandler(storage -> storage.result().put("KEY", "VALUE", result -> {
            test.assertTrue(result.succeeded());
            async.complete();
        }));

        StorageLoader.load(future, STORAGE_HAZELMAP, Strings.MAP_ACCOUNTS);
    }
}
