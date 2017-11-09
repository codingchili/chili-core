package com.codingchili.core.storage;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;
import io.vertx.core.Future;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Robin Duda
 * <p>
 * Tests the loading of the available storage plugins.
 */
@RunWith(VertxUnitRunner.class)
public class StorageLoaderIT {
    private static final String TEST_DB = "test";
    private static CoreContext context;
    @Rule
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

    @BeforeClass
    public static void setUp() {
        context = new SystemContext();
    }

    @AfterClass
    public static void tearDown(TestContext test) {
        context.close(test.asyncAssertSuccess());
    }

    @Test
    public void testLoadWithFail(TestContext test) {
        Async async = test.async();
        new StorageLoader<>(context)
                .withDB("", "")
                .withValue(Storable.class)
                .withPlugin("null").build(done -> {
            if (done.failed()) {
                async.complete();
            } else {
                test.fail("Expected future to fail.");
            }
        });
    }

    @Test
    public void testLoadLocalAsyncMap(TestContext test) {
        loadStoragePlugin(test, PrivateMap.class);
    }

    @Test
    public void testLoadJsonMap(TestContext test) {
        loadStoragePlugin(test, JsonMap.class);
    }

    @Test
    public void testLoadIndexedMapV(TestContext test) {
        loadStoragePlugin(test, IndexedMapVolatile.class);
    }

    @Test
    public void testLoadIndexedMapP(TestContext test) {
        loadStoragePlugin(test, IndexedMapPersisted.class);
    }

    @Test
    public void testLoadSharedMap(TestContext test) {
        loadStoragePlugin(test, SharedMap.class);
    }

    @Ignore("Requires clustering - not interesting in this test case.")
    @Test
    public void testLoadHazelAsyncMap(TestContext test) {
        loadStoragePlugin(test, HazelMap.class);
    }

    @Ignore("Requires an available ElasticSearch database.")
    @Test
    public void testLoadElasticMap(TestContext test) {
        loadStoragePlugin(test, ElasticMap.class);
    }

    @Ignore("Requires an available MongoDB database.")
    @Test
    public void testLoadMongoMap(TestContext test) {
        loadStoragePlugin(test, MongoDBMap.class);

    }

    private void loadStoragePlugin(TestContext test, Class<? extends AsyncStorage> plugin) {
        Future<AsyncStorage<StorableString>> future = Future.future();
        Async async = test.async();

        new StorageLoader<StorableString>(context)
                .withPlugin(plugin)
                .withDB(TEST_DB, UUID.randomUUID().toString())
                .withValue(StorableString.class)
                .build(future);

        future.setHandler(done -> {
            if (done.succeeded()) {
                async.complete();
            }
        });
    }

    private class StorableString implements Storable {
        @Override
        public String id() {
            return "";
        }
    }
}
