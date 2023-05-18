package com.codingchili.core.storage;

import io.vertx.core.*;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.context.TimerSource;
import com.codingchili.core.testing.StorageObject;


/**
 * Tests the stale handler for realms.
 */
@RunWith(VertxUnitRunner.class)
public class EntryWatcherTest {
    private static final String TEST_NAME = "TEST_NAME";
    private static final String DB = "entrywatcher";
    private static final String COLLECTION = "test";
    private static final String LEVEL = "level";
    private static final int WAIT_MS = 1000;
    private static final int REMOVE_INTERVAL = 50;
    private static final int LEVEL_PERSIST = 50;
    private static final int LEVEL_REMOVE = 0;
    @Rule
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);
    private AsyncStorage<StorageObject> storage;
    private StorageObject object = new StorageObject(TEST_NAME, 5);
    private StorageContext context;

    @Before
    public void setUp(TestContext test) {
        Async async = test.async();
        context = new StorageContext();

        createStorage(result -> {
            if (result.succeeded()) {
                this.storage = result.result();

                getQuery().poll(entry -> entry.forEach(item -> {
                    storage.remove(item.getId(), removed -> {
                        if (removed.failed()) {
                            test.fail(removed.cause());
                        }
                    });
                }), TimerSource.of(REMOVE_INTERVAL));

                storage.put(object, put -> {
                    test.assertTrue(put.succeeded());
                    async.complete();
                });
            } else {
                test.fail(result.cause());
            }
        });
    }

    private QueryBuilder<StorageObject> getQuery() {
        return storage.query(LEVEL).between(Long.MIN_VALUE, 0L)
                .or(LEVEL).between(100L, Long.MAX_VALUE);
    }

    private void createStorage(Handler<AsyncResult<AsyncStorage<StorageObject>>> future) {
        new StorageLoader<StorageObject>(context)
                .withPlugin(JsonMap.class)
                .withValue(StorageObject.class)
                .withDB(DB, COLLECTION)
                .build(future);
    }

    @After
    public void tearDown(TestContext test) {
        context.close(test.asyncAssertSuccess());
    }

    private void setPersist() {
        object.setLevel(LEVEL_PERSIST);
        storage.update(object, result -> {
        });
    }

    private void setRemove(TestContext test) {
        object.setLevel(LEVEL_REMOVE);
        storage.update(object, result -> {
            if (result.failed()) {
                test.fail(result.cause());
            }
        });
    }

    @Test
    public void testItemRemovedOnHandler(TestContext test) {
        Async async = test.async();
        setRemove(test);

        context.timer(WAIT_MS, handler -> storage.get(TEST_NAME, event -> {
            test.assertFalse(event.succeeded());
            test.assertNull(event.result());
            async.complete();
        }));
    }

    @Test
    public void testRealmNotRemovedWhenNotStale(TestContext test) {
        Async async = test.async();
        setPersist();

        context.timer(WAIT_MS, handler -> storage.get(TEST_NAME, get -> {
            test.assertTrue(get.succeeded());
            test.assertNotNull(get.result());
            async.complete();
        }));
    }
}
