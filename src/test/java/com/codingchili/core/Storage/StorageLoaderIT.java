package com.codingchili.core.storage;

import io.vertx.core.*;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.storage.internal.*;

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
        loadStoragePlugin(test.async(), PrivateMap.class);
    }

    @Test
    public void testLoadJsonMap(TestContext test) {
        loadStoragePlugin(test.async(), JsonMap.class);

    }

    @Test
    public void testLoadSharedMap(TestContext test) {
        loadStoragePlugin(test.async(), SharedMap.class);
    }

    @Test
    public void testLoadHazelAsyncMap(TestContext test) {
        loadStoragePlugin(test.async(), HazelMap.class);
    }

    @Ignore("Requires an available ElasticSearch database.")
    @Test
    public void testLoadElasticMap(TestContext test) {
        loadStoragePlugin(test.async(), ElasticMap.class);
    }

    @Test
    public void testLoadMongoMap(TestContext test) {
        loadStoragePlugin(test.async(), MongoDBMap.class);

    }

    private void loadStoragePlugin(Async async, Class plugin) {
        Future<AsyncStorage<String, String>> future = Future.future();

        future.setHandler(completed -> async.complete());

        StorageLoader.prepare()
                .withContext(context)
                .withPlugin(plugin)
                .withDB(TEST_MAP)
                .withCollection(TEST_COLLECTION)
                .withClass(PrivateMapTest.class)
                .build(future);
    }
}
