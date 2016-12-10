package com.codingchili.realmregistry;

import com.codingchili.realmregistry.configuration.RealmSettings;
import com.codingchili.realmregistry.model.*;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.time.Instant;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.storage.PrivateMap;


/**
 * @author Robin Duda
 *         <p>
 *         Tests the stale handler for realms.
 */
@RunWith(VertxUnitRunner.class)
public class StaleRealmHandlerTest {
    private static final String REALM_NAME = "REALM_NAME";
    private static final int STALE_TIMEOUT = 150;
    private static AsyncRealmStore realms;
    private static WritableContextMock context;

    @Before
    public void setUp() {
        context = new WritableContextMock(Vertx.vertx());
        realms = new RealmDB(new PrivateMap<>(new StorageContext<>(context)));
        context.timeout = STALE_TIMEOUT;

        StaleRealmHandler.watch(context, realms);
    }

    @After
    public void tearDown(TestContext test) {
        StaleRealmHandler.stop();
        context.vertx().close(test.asyncAssertSuccess());
    }

    private void updateRealm() {
        realms.put(Future.future(), new RealmSettings()
                .setName(REALM_NAME)
                .setUpdated(Instant.now().toEpochMilli() + context.timeout * 2));
    }

    private void setRealmStale() {
        realms.put(Future.future(), new RealmSettings()
                .setName(REALM_NAME)
                .setUpdated(Instant.now().toEpochMilli() - context.timeout * 2));
    }

    @Test
    public void testRealmRemovedWhenStale(TestContext test) {
        Async async = test.async();
        setRealmStale();

        context.timer(context.realmTimeout() + 100, handler -> {
            Future<RealmSettings> future = Future.future();

            future.setHandler(event -> {
                test.assertFalse(event.succeeded());
                test.assertNull(event.result());
                async.complete();
            });

            realms.get(future, REALM_NAME);
        });
    }

    @Test
    public void testRealmNotRemovedWhenNotStale(TestContext test) {
        updateRealm();
        assertRealmExistsAfterMS(test, test.async(), context.timeout * 2);
    }

    @Ignore("Disabled: waiting for issue to move StaleHandler to core")
    @Test
    public void testTimeoutUpdatesWithConfiguration(TestContext test) {
        Async async = test.async();

        updateRealm();
        context.timeout = 500;

        context.timer(200, event -> {
            setRealmStale();
            assertRealmExistsAfterMS(test, async, 200);
        });
    }

    private void assertRealmExistsAfterMS(TestContext test, Async async, int ms) {
        context.timer(ms, handler -> {
            Future<RealmSettings> future = Future.future();

            future.setHandler(event -> {
                test.assertTrue(event.succeeded());
                test.assertNotNull(event.result());
                async.complete();
            });

            realms.get(future, REALM_NAME);
        });
    }

    private static class WritableContextMock extends ContextMock {
        public int timeout = STALE_TIMEOUT;

        WritableContextMock(Vertx vertx) {
            super(vertx);
        }

        @Override
        public int realmTimeout() {
            return timeout;
        }
    }
}
