package com.codingchili.core.testing;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.storage.*;
import com.codingchili.core.storage.exception.*;


/**
 * @author Robin Duda
 *         <p>
 *         Common test cases for the map implementations.
 */
@Ignore
@RunWith(VertxUnitRunner.class)
public class MapTestCases {
    protected StorageContext<JsonObject> context;
    private static final JsonObject VALUE = new JsonObject().put("value", "value");
    private static final JsonObject VALUE_OTHER = new JsonObject().put("value", "other");
    private static final String KEY = "key";
    private static final String DB_NAME = "spinach";
    private static final String COLLECTION = "leaves";
    private AsyncStorage<String, JsonObject> store;

    @Rule
    public Timeout timeout = Timeout.seconds(6);

    @Before
    public void setUp(TestContext test) {
        setUp(test, SharedMap.class);
    }

    public void setUp(Async async, Class plugin, Vertx vertx) {
        Future<AsyncStorage<String, JsonObject>> future = Future.future();

        context = new StorageContext<>(vertx);

        future.setHandler(map -> {
            store = map.result();
            map.result().clear(clear -> async.complete());
        });

        StorageLoader.prepare()
                .withDB(DB_NAME)
                .withCollection(COLLECTION)
                .withClass(JsonObject.class)
                .withContext(context)
                .withPlugin(plugin)
                .build(future);
    }

    protected void setUp(TestContext test, Class plugin) {
        setUp(test.async(), plugin, Vertx.vertx());
    }

    @After
    public void tearDown(TestContext test) {
        context.vertx().close(test.asyncAssertSuccess());
    }

    @Test
    public void testGet(TestContext test) {
        Async async = test.async();

        store.put(KEY, VALUE, put -> {
            test.assertTrue(put.succeeded());

            store.get(KEY, get -> {
                test.assertTrue(get.succeeded());
                test.assertEquals(VALUE, get.result());
                async.complete();
            });
        });
    }

    @Test
    public void testGetMissing(TestContext test) {
        Async async = test.async();

        store.get(KEY, get -> {
            test.assertTrue(get.failed());
            test.assertNull(get.result());
            test.assertEquals(MissingEntityException.class, get.cause().getClass());
            async.complete();
        });
    }

    @Test
    public void testPut(TestContext test) {
        Async async = test.async();

        store.put(KEY, VALUE, put -> {
            test.assertTrue(put.succeeded());

            store.get(KEY, get -> {
                test.assertEquals(VALUE, get.result());
                test.assertTrue(get.succeeded());
                async.complete();
            });
        });
    }

    @Test
    public void testPutWithTTL(TestContext test) {
        Async async = test.async();

        store.put(KEY, VALUE, 50, put -> {
            test.assertTrue(put.succeeded());
            waitForExpiry(test, async);
        });
    }

    private void waitForExpiry(TestContext test, Async async) {
        context.timer(800, event -> store.get(KEY, get -> {
            test.assertTrue(get.failed());
            async.complete();
        }));
    }

    @Test
    public void testPutIfAbsent(TestContext test) {
        Async async = test.async();

        store.putIfAbsent(KEY, VALUE, put -> {
            test.assertTrue(put.succeeded());

            store.get(KEY, get -> {
                test.assertTrue(get.succeeded());
                async.complete();
            });
        });
    }

    @Test
    public void testPutIfAbsentTTL(TestContext test) {
        Async async = test.async();

        store.putIfAbsent(KEY, VALUE, 50, put -> {
            test.assertTrue(put.succeeded());
            waitForExpiry(test, async);
        });
    }

    @Test
    public void testPutIfAbsentNotAbsent(TestContext test) {
        Async async = test.async();

        store.putIfAbsent(KEY, VALUE, outer -> {
            test.assertTrue(outer.succeeded());

            store.putIfAbsent(KEY, VALUE_OTHER, inner -> {
                test.assertTrue(inner.failed());
                test.assertEquals(ValueAlreadyPresentException.class, inner.cause().getClass());
                async.complete();
            });
        });
    }

    @Test
    public void testRemove(TestContext test) {
        Async async = test.async();

        store.put(KEY, VALUE, outer -> {
            test.assertTrue(outer.succeeded());

            store.remove(KEY, inner -> {
                test.assertTrue(inner.succeeded());

                store.get(KEY, result -> {
                    test.assertTrue(result.failed());
                    async.complete();
                });
            });
        });
    }

    @Test
    public void testRemoveNotPresent(TestContext test) {
        Async async = test.async();

        store.remove(KEY, remove -> {
            test.assertTrue(remove.failed());
            test.assertEquals(NothingToRemoveException.class, remove.cause().getClass());
            async.complete();
        });
    }

    @Test
    public void testReplace(TestContext test) {
        Async async = test.async();

        store.put(KEY, VALUE, result -> {
            test.assertTrue(result.succeeded());

            store.replace(KEY, VALUE_OTHER, replace -> {
                test.assertTrue(replace.succeeded());

                store.get(KEY, get -> {
                    test.assertEquals(VALUE_OTHER, get.result());
                    test.assertTrue(get.succeeded());
                    async.complete();
                });
            });
        });
    }

    @Test
    public void testReplaceIfNotPresent(TestContext test) {
        Async async = test.async();

        store.replace(KEY, VALUE, replace -> {
            test.assertTrue(replace.failed());
            test.assertEquals(NothingToReplaceException.class, replace.cause().getClass());
            async.complete();
        });
    }

    @Test
    public void testClear(TestContext test) {
        Async async = test.async();

        store.put(KEY, VALUE, put -> {
            test.assertTrue(put.succeeded());

            store.clear(clear -> {
                test.assertTrue(clear.succeeded());

                store.size(size -> {
                    test.assertEquals(0, size.result());
                    test.assertTrue(size.succeeded());
                    async.complete();
                });
            });
        });
    }

    @Test
    public void testSize(TestContext test) {
        Async async = test.async();

        store.put(KEY, VALUE, put -> {
            test.assertTrue(put.succeeded());

            store.size(size -> {
                test.assertEquals(1, size.result());
                test.assertTrue(size.succeeded());
                async.complete();
            });
        });
    }

    @Test
    public void testQueryExact() {

    }

    @Test
    public void testQuerySimilar() {

    }

    @Test
    public void testQueryRange() {

    }
}
