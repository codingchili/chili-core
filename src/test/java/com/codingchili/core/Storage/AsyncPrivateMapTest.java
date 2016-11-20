package com.codingchili.core.Storage;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import com.codingchili.core.Context.CoreContext;
import com.codingchili.core.Testing.ContextMock;

/**
 * @author Robin Duda
 *         <p>
 *         Tests for the storage providers in core. Reuse these tests when new
 *         storage subsystems are implemented using the StorageLoader.
 */
@RunWith(VertxUnitRunner.class)
public class AsyncPrivateMapTest {
    private static final JsonObject VALUE = new JsonObject().put("value", "value");
    private static final JsonObject VALUE_OTHER = new JsonObject().put("value", "other");
    private static final String KEY = "key";
    private static final String TEST = "test";
    private AsyncStorage<String, JsonObject> store;
    private CoreContext context;

    @Rule
    public Timeout timeout = Timeout.seconds(5);

    @Before
    public void setUp(TestContext test) {
        Async async = test.async();

        Future<AsyncStorage<String, JsonObject>> future = Future.future();

        //Vertx.clusteredVertx(new VertxOptions(), clustered -> {
            context = new ContextMock(Vertx.vertx());

            future.setHandler(result -> {
                store = result.result();
                store.clear(clear -> async.complete());
            });

            StorageLoader.prepare()
                    .withDB(TEST)
                    .withClass(JsonObject.class)
                    .withContext(context)
                    .withPlugin(AsyncPrivateMap.class)
                    .build(future);
        //});
    }

    @After
    public void tearDown(TestContext test) {
        context.vertx().close(test.asyncAssertSuccess());
    }

    @Test
    public void testPut(TestContext test) {
        Async async = test.async();

        store.put(KEY, VALUE, put -> store.get(KEY, get -> {
            test.assertEquals(VALUE, get.result());
            test.assertTrue(get.succeeded());
            async.complete();
        }));
    }

    @Test
    public void testPutWithTTL(TestContext test) {
        Async async = test.async();
        store.put(KEY, VALUE, 1, put -> waitForExpiry(test, async));
    }

    private void waitForExpiry(TestContext test, Async async) {
        context.timer(800, event -> store.get(KEY, get -> {
            test.assertNull(get.result());
            test.assertTrue(get.succeeded());
            async.complete();
        }));
    }

    @Test
    public void testPutIfAbsent(TestContext test) {
        Async async = test.async();

        store.putIfAbsent(KEY, VALUE, put -> store.get(KEY, get -> {
            test.assertEquals(VALUE, get.result());
            test.assertTrue(get.succeeded());
            async.complete();
        }));
    }

    @Test
    public void testPutIfAbsentTTL(TestContext test) {
        Async async = test.async();
        store.putIfAbsent(KEY, VALUE, 1, handler -> waitForExpiry(test, async));
    }

    @Test
    public void testPutIfAbsentNotAbsent(TestContext test) {
        Async async = test.async();

        store.putIfAbsent(KEY, VALUE, outer -> {
            test.assertNull(outer.result());

            store.putIfAbsent(KEY, VALUE_OTHER, inner -> {
                test.assertEquals(VALUE, inner.result());
                test.assertTrue(inner.succeeded());
                async.complete();
            });
        });
    }

    @Test
    public void testRemove(TestContext test) {
        Async async = test.async();

        store.put(KEY, VALUE, outer -> {
            store.remove(KEY, inner -> {
                test.assertEquals(VALUE, inner.result());
                test.assertTrue(inner.succeeded());
                async.complete();
            });
        });
    }

    @Test
    public void testRemoveNonExisting(TestContext test) {
        Async async = test.async();

        store.put(KEY, VALUE, put -> {
            test.assertEquals(null, put.result());
            test.assertTrue(put.succeeded());
            async.complete();
        });
    }

    @Test
    public void testRemoveIfPresent(TestContext test) {
        Async async = test.async();

        store.put(KEY, VALUE, put -> {
            store.removeIfPresent(KEY, VALUE, remove -> {
                test.assertTrue(remove.result());
                test.assertTrue(remove.succeeded());
                async.complete();
            });
        });
    }

    @Test
    public void testRemoveIfPresentNotPresent(TestContext test) {
        Async async = test.async();

        store.removeIfPresent(KEY, VALUE, remove -> {
            test.assertFalse(remove.result());
            test.assertTrue(remove.succeeded());
            async.complete();
        });
    }

    @Test
    public void testRemoveIfAnotherValuePresent(TestContext test) {
        Async async = test.async();

        store.put(KEY, VALUE_OTHER, put -> store.removeIfPresent(KEY, VALUE, remove -> {
            test.assertFalse(remove.result());
            test.assertTrue(remove.succeeded());
            async.complete();
        }));
    }

    @Test
    public void testReplace(TestContext test) {
        Async async = test.async();

        store.put(KEY, VALUE, put -> store.replace(KEY, VALUE_OTHER, replace -> {
            test.assertEquals(VALUE, replace.result());
            test.assertTrue(replace.succeeded());
            async.complete();
        }));
    }

    @Test
    public void testReplaceIfPresent(TestContext test) {
        Async async = test.async();

        store.put(KEY, VALUE, put -> store.replaceIfPresent(KEY, VALUE, VALUE_OTHER, replace -> {
            test.assertTrue(replace.result());
            test.assertTrue(replace.succeeded());
            async.complete();
        }));
    }

    @Test
    public void testReplaceIfNotPresent(TestContext test) {
        Async async = test.async();

        store.replaceIfPresent(KEY, VALUE, VALUE_OTHER, replace -> {
            test.assertFalse(replace.result());
            test.assertTrue(replace.succeeded());
            async.complete();
        });
    }

    @Test
    public void testReplaceIfAnotherValuePresent(TestContext test) {
        Async async = test.async();

        store.put(KEY, VALUE, put -> store.replaceIfPresent(KEY, VALUE_OTHER, VALUE_OTHER, replace -> {
            test.assertFalse(replace.result());
            test.assertTrue(replace.succeeded());
            async.complete();
        }));
    }

    @Test
    public void testClear(TestContext test) {
        Async async = test.async();

        store.put(KEY, VALUE, put -> store.clear(clear -> store.size(size -> {
            test.assertEquals(0, size.result());
            test.assertTrue(size.succeeded());
            async.complete();
        })));
    }

    @Test
    public void testSize(TestContext test) {
        Async async = test.async();

        store.put(KEY, VALUE, put -> {
            store.size(size -> {
                test.assertEquals(1, size.result());
                test.assertTrue(size.succeeded());
                async.complete();
            });
        });

    }
}
