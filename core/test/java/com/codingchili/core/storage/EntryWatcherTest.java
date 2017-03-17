package com.codingchili.core.storage;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.testing.ContextMock;
import com.codingchili.core.testing.StorageObject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;


/**
 * @author Robin Duda
 *         <p>
 *         Tests the stale handler for realms.
 */
@RunWith(VertxUnitRunner.class)
public class EntryWatcherTest {
    private static final String TEST_NAME = "TEST_NAME";
    private static final String DB = "db";
    private static final String COLLECTION = "collection";
    private static final String ATTRIBUTE = "level";
    private static final int WAIT_MS = 500;
    private final int INTERVAL = 150;
    private AsyncStorage<StorageObject> storage;
    private StorageObject object = new StorageObject(TEST_NAME, 5);
    private StorageContext context;
    private EntryWatcher<StorageObject> watcher;

    @Rule
    public Timeout timeout = new Timeout(3, TimeUnit.SECONDS);

    @Before
    public void setUp(TestContext test) {
        Async async = test.async();
        context = new StorageContext(Vertx.vertx());

        createStorage(result -> {
            if (result.succeeded()) {
                this.storage = result.result();

                this.watcher = new EntryWatcher<>(storage, getQuery(), () -> INTERVAL);
                watcher.start(entry -> storage.remove(entry.id(), removed -> {
                    System.err.println("removed " + entry.id());
                }));

                storage.put(object, put -> {});
                async.complete();
            } else {
                test.fail(result.cause());
            }
        });
    }

    private ReusableQueryBuilder<StorageObject> getQuery() {
        return new ReusableQueryBuilder<StorageObject>(ATTRIBUTE).between(1L, 100L);
    }

    private void createStorage(Handler<AsyncResult<AsyncStorage<StorageObject>>> future) {
        new StorageLoader<StorageObject>().jsonmap(context)
                .withClass(StorageObject.class)
                .withDB(DB, COLLECTION)
                .build(result -> {
                    if (result.succeeded()) {
                        future.handle(Future.succeededFuture(result.result()));
                    } else {
                        future.handle(Future.failedFuture(result.cause()));
                    }
                });
    }

    @After
    public void tearDown(TestContext test) {
        context.vertx().close(test.asyncAssertSuccess());
    }

    private void setPersist() {
        object.setLevel(50);
        storage.update(object, result -> {
        });
    }

    private void setRemove() {
        object.setLevel(0);
        storage.update(object, result -> {
        });
    }

    @Test
    public void testItemRemovedOnHandler(TestContext test) {
        Async async = test.async();
        /*setRemove();

        context.timer(WAIT_MS, handler -> {
            storage.get(TEST_NAME, event -> {
                test.assertFalse(event.succeeded());
                test.assertNull(event.result());
                async.complete();
            });
        });*/
    }

    @Test
    public void testRealmNotRemovedWhenNotStale(TestContext test) {
        setPersist();
        //assertRealmExistsAfterMS(test, test.async(), context.timeout * 2);
    }

    @Ignore("Disabled: waiting for issue to move StaleHandler to core")
    @Test
    public void testTimeoutUpdatesWithConfiguration(TestContext test) {
       /* Async async = test.async();

        setPersist();
        context.timeout = 500;

        context.timer(200, event -> {
            setRealmStale();
            assertRealmExistsAfterMS(test, async, 200);
        });*/
    }

    private void assertRealmExistsAfterMS(TestContext test, Async async, int ms) {
       /* context.timer(ms, handler -> {
            storage.get(event -> {
                test.assertTrue(event.succeeded());
                test.assertNotNull(event.result());
                async.complete();
            }, TEST_NAME);
        });*/
    }
}
