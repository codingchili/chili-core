package com.codingchili.core.storage;

import java.util.concurrent.TimeUnit;

import org.junit.*;
import org.junit.runner.RunWith;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * @author Robin Duda
 *         <p>
 *         Tests the loading of the available storage plugins.
 */
@RunWith(VertxUnitRunner.class)
public class StorageLoaderIT {
    private static CoreContext context;
    private static final String TEST_MAP = "test";
    private static final String TEST_COLLECTION = "collection";

    @Rule
    public Timeout timeout = new Timeout(8, TimeUnit.SECONDS);

    @BeforeClass
    public static void setUp(TestContext test) {
        Async async = test.async();

        Vertx.clusteredVertx(new VertxOptions(), vertx -> {
            context = new SystemContext(vertx.result());
            async.complete();
        });
    }

    @AfterClass
    public static void tearDown(TestContext test) {
        context.vertx().close(test.asyncAssertSuccess());
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
    public void testLoadIndexedMap(TestContext test) {
        loadStoragePlugin(test.async(), IndexedMap.class);
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

    @Ignore("Requires an available MongoDB database.")
    @Test
    public void testLoadMongoMap(TestContext test) {
        loadStoragePlugin(test.async(), MongoDBMap.class);

    }

    private void loadStoragePlugin(Async async, Class plugin) {
        new StorageLoader<StorableString>(context)
                .withPlugin(plugin)
                .withDB(TEST_MAP, TEST_COLLECTION)
                .withClass(StorableString.class)
                .build(storage -> async.complete());
    }

    private class StorableString implements Storable {
        @Override
        public String id() {
            return "";
        }
    }
}
