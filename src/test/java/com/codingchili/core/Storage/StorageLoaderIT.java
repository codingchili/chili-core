package com.codingchili.core.Storage;

import io.vertx.core.*;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.codingchili.core.Context.CoreContext;
import com.codingchili.core.Context.SystemContext;

/**
 * @author Robin Duda
 *
 * Tests the loading of the available storage plugins.
 */
@RunWith(VertxUnitRunner.class)
public class StorageLoaderIT {
    private static CoreContext context;
    private static final String TEST_MAP = "test";
    private static final String TEST_COLLECTION = "collection";

    @BeforeClass
    public static void setUp(TestContext test) {
        Async async = test.async();

        Vertx.clusteredVertx(new VertxOptions(), vertx -> {
            context = new SystemContext(vertx.result());
            async.complete();
        });
    }

    @Test
    public void testLoadLocalAsyncMap(TestContext test) {
        loadStoragePlugin(test.async(), AsyncPrivateMap.class);
    }

    @Test
    public void testLoadJsonMap(TestContext test) {
        loadStoragePlugin(test.async(), AsyncJsonMap.class);

    }

    @Test
    public void testLoadSharedMap(TestContext test) {
        loadStoragePlugin(test.async(), AsyncSharedMap.class);
    }

    @Test
    public void testLoadHazelAsyncMap(TestContext test) {
        loadStoragePlugin(test.async(), AsyncHazelMap.class);
    }

    @Test
    public void testLoadElasticMap(TestContext test) {
        loadStoragePlugin(test.async(), AsyncElasticMap.class);
    }

    @Test
    public void testLoadMongoMap(TestContext test) {
        loadStoragePlugin(test.async(), AsyncMongoMap.class);

    }

    private void loadStoragePlugin(Async async, Class plugin) {
        Future<AsyncStorage<String, String>> future = Future.future();

        future.setHandler(completed -> async.complete());

        StorageLoader.prepare()
                .withContext(context)
                .withPlugin(plugin)
                .withDB(TEST_MAP)
                .withCollection(TEST_COLLECTION)
                .withClass(AsyncPrivateMapTest.class)
                .build(future);
    }
}
