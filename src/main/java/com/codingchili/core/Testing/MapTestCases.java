package com.codingchili.core.testing;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.files.Configurations;
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
    protected StorageContext<StorageObject> context;
    protected AsyncStorage<String, StorageObject> store;
    private static final String LEVEL = "level";
    private static final String ID = "id";
    private static final String ONE = "one";
    private static final String TWO = "two";
    private static final String THREE = "three";
    private static final String KEY_MISSING = "KEY_MISSING";
    private static final StorageObject OBJECT_ONE = new StorageObject("one", 1);
    private static final StorageObject OBJECT_TWO = new StorageObject("two", 2);
    private static final StorageObject OBJECT_THREE = new StorageObject("three", 3);
    private static final String DB_NAME = "spinach";
    private static final String COLLECTION = "leaves";

    @Rule
    public Timeout timeout = Timeout.seconds(6);

    @Before
    public void setUp(TestContext test) {
        setUp(test, PrivateMap.class);
    }

    protected void setUp(TestContext test, Class plugin) {
        setUp(test.async(), plugin, Vertx.vertx());
    }

    public void setUp(Async async, Class plugin, Vertx vertx) {
        Future<AsyncStorage<String, StorageObject>> future = Future.future();

        context = new StorageContext<>(vertx);

        future.setHandler(map -> {
            store = map.result();
            prepareStore(async);
        });

        StorageLoader.prepare()
                .withDB(DB_NAME)
                .withCollection(COLLECTION)
                .withClass(StorageObject.class)
                .withContext(context)
                .withPlugin(plugin)
                .build(future);
    }

    private void prepareStore(Async async) {
        store.clear(result -> {
            store.put(TWO, OBJECT_TWO, object -> {
                store.put(THREE, OBJECT_THREE, other -> {
                    async.complete();
                });
            });
        });
    }

    @After
    public void tearDown(TestContext test) {
        context.vertx().close(test.asyncAssertSuccess());
    }

    @Test
    public void testGet(TestContext test) {
        Async async = test.async();

        store.get(TWO, get -> {
            test.assertTrue(get.succeeded());
            test.assertEquals(OBJECT_TWO, get.result());
            async.complete();
        });
    }

    @Test
    public void testGetMissing(TestContext test) {
        Async async = test.async();

        store.get(KEY_MISSING, get -> {
            test.assertTrue(get.failed());
            test.assertNull(get.result());
            test.assertEquals(ValueMissingException.class, get.cause().getClass());
            async.complete();
        });
    }

    @Test
    public void testPut(TestContext test) {
        Async async = test.async();

        store.put(ONE, OBJECT_ONE, put -> {
            test.assertTrue(put.succeeded());

            store.get(ONE, get -> {
                test.assertEquals(OBJECT_ONE, get.result());
                test.assertTrue(get.succeeded());
                async.complete();
            });
        });
    }

    @Test
    public void testPutWithTTL(TestContext test) {
        Async async = test.async();

        store.put(ONE, OBJECT_ONE, 50, put -> {
            test.assertTrue(put.succeeded());
            waitForExpiry(test, async);
        });
    }

    private void waitForExpiry(TestContext test, Async async) {
        context.timer(800, event -> store.get(ONE, get -> {
            test.assertTrue(get.failed());
            async.complete();
        }));
    }

    @Test
    public void testPutIfAbsent(TestContext test) {
        Async async = test.async();

        store.putIfAbsent(ONE, OBJECT_ONE, put -> {
            test.assertTrue(put.succeeded());

            store.get(ONE, get -> {
                test.assertTrue(get.succeeded());
                async.complete();
            });
        });
    }

    @Test
    public void testPutIfAbsentTTL(TestContext test) {
        Async async = test.async();

        store.putIfAbsent(ONE, OBJECT_ONE, 50, put -> {
            test.assertTrue(put.succeeded());
            waitForExpiry(test, async);
        });
    }

    @Test
    public void testPutIfAbsentNotAbsent(TestContext test) {
        Async async = test.async();

        store.putIfAbsent(TWO, OBJECT_TWO, inner -> {
            test.assertTrue(inner.failed());
            test.assertEquals(ValueAlreadyPresentException.class, inner.cause().getClass());
            async.complete();
        });
    }

    @Test
    public void testRemove(TestContext test) {
        Async async = test.async();

        store.remove(TWO, inner -> {
            test.assertTrue(inner.succeeded());

            store.get(TWO, result -> {
                test.assertTrue(result.failed());
                async.complete();
            });
        });
    }

    @Test
    public void testRemoveNotPresent(TestContext test) {
        Async async = test.async();

        store.remove(KEY_MISSING, remove -> {
            test.assertTrue(remove.failed());
            test.assertEquals(NothingToRemoveException.class, remove.cause().getClass());
            async.complete();
        });
    }

    @Test
    public void testReplace(TestContext test) {
        Async async = test.async();

        store.replace(TWO, OBJECT_ONE, replace -> {
            test.assertTrue(replace.succeeded());

            store.get(TWO, get -> {
                test.assertEquals(OBJECT_ONE, get.result());
                test.assertTrue(get.succeeded());
                async.complete();
            });
        });
    }

    @Test
    public void testReplaceIfNotPresent(TestContext test) {
        Async async = test.async();

        store.replace(KEY_MISSING, OBJECT_ONE, replace -> {
            test.assertTrue(replace.failed());
            test.assertEquals(NothingToReplaceException.class, replace.cause().getClass());
            async.complete();
        });
    }

    @Test
    public void testClear(TestContext test) {
        Async async = test.async();

        store.clear(clear -> {
            test.assertTrue(clear.succeeded());

            store.size(size -> {
                test.assertEquals(0, size.result());
                test.assertTrue(size.succeeded());
                async.complete();
            });
        });
    }

    @Test
    public void testSize(TestContext test) {
        Async async = test.async();

        store.size(size -> {
            test.assertEquals(2, size.result());
            test.assertTrue(size.succeeded());
            async.complete();
        });
    }

    @Test
    public void testQueryExact(TestContext test) {
        Async async = test.async();

        store.queryExact(ID, TWO, query -> {
            test.assertTrue(query.succeeded());
            test.assertEquals(1, query.result().size());
            async.complete();
        });
    }

    @Test
    public void testQueryMatchNone(TestContext test) {
        Async async = test.async();

        store.queryExact(ID, ONE, query -> {
            test.assertTrue(query.succeeded());
            test.assertEquals(0, query.result().size());
            async.complete();
        });
    }

    @Test
    public void testQuerySimilar(TestContext test) {
        Async async = test.async();

        store.querySimilar(ID, "thr", query -> {
            test.assertTrue(query.succeeded());
            test.assertEquals(1, query.result().size());


            store.querySimilar(ID, "rht", inner -> {
                test.assertTrue(inner.succeeded());
                test.assertEquals(0, inner.result().size());
                async.complete();
            });
        });
    }

    @Test
    public void testQuerySimilarTooShortExpression(TestContext test) {
        Async async = test.async();

        // don't test if set to 2 or less, default is 3.
        if (Configurations.storage().getMinFeedbackChars() > 2) {
            store.querySimilar(ID, "th", query -> {
                test.assertTrue(query.succeeded());
                test.assertEquals(0, query.result().size());
                async.complete();
            });
        }
    }

    @Test
    public void testQuerySimilarInvalidExpression(TestContext test) {
        Async async = test.async();

        store.querySimilar(ID, "val.*", query -> {
            test.assertTrue(query.succeeded());
            test.assertEquals(0, query.result().size());
            async.complete();
        });
    }

    @Test
    public void testQueryRange(TestContext test) {
        Async async = test.async();

        store.queryRange(LEVEL, 2, 3, query -> {
            test.assertTrue(query.succeeded());
            test.assertEquals(2, query.result().size());

            query.result().stream().forEach(item -> {
                test.assertTrue(item.getLevel() == 2 || item.getLevel() == 3);
            });

            async.complete();
        });
    }

    @Test
    public void testQueryRangeNoMatches(TestContext test) {
        Async async = test.async();

        store.queryRange(LEVEL, 100, 200, query -> {
            test.assertTrue(query.succeeded());
            test.assertEquals(0, query.result().size());
            async.complete();
        });
    }
}
