package com.codingchili.services.Authentication.Model;

import io.vertx.core.Future;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.time.Instant;

import com.codingchili.core.Testing.AsyncMapMock;

import com.codingchili.services.Realm.Configuration.RealmSettings;

/**
 * @author Robin Duda
 */
@RunWith(VertxUnitRunner.class)
public class StaleRealmHandlerTest {
    private static final String REALM_NAME = "REALM_NAME";
    private static final int STALE_TIMEOUT = 250;
    private static AsyncRealmStore realms;
    private static WritableContextMock mock;

    @Before
    public void setUp() {
        realms = new HazelRealmDB(new AsyncMapMock<>());

        mock = new WritableContextMock();

        StaleRealmHandler.watch(mock, realms);
    }

    @After
    public void tearDown() {
        StaleRealmHandler.stop();
    }

    private void updateRealm() {
        realms.put(Future.future(), new RealmSettings()
                .setName(REALM_NAME)
                .setUpdated(Instant.now().toEpochMilli() + mock.timeout * 2));
    }

    private void setRealmStale() {
        realms.put(Future.future(), new RealmSettings()
                .setName(REALM_NAME)
                .setUpdated(Instant.now().toEpochMilli() - mock.timeout * 2));
    }

    @Test
    public void testRealmRemovedWhenStale(TestContext context) {
        Async async = context.async();
        setRealmStale();

        mock.vertx().setTimer(mock.realmTimeout() + 400, handler -> {
            Future<RealmSettings> future = Future.future();

            future.setHandler(event -> {
                context.assertTrue(event.succeeded());
                context.assertNull(event.result());
                async.complete();
            });

            realms.get(future, REALM_NAME);
        });
    }

    @Test
    public void testRealmNotRemovedWhenNotStale(TestContext context) {
        updateRealm();
        assertRealmExistsAfterMS(context, context.async(), mock.timeout + 400);
    }

    @Test
    public void testTimeoutUpdatesWithConfiguration(TestContext context) {
        Async async = context.async();
        updateRealm();
        mock.timeout = 1000;

        mock.vertx().setTimer(325, event -> {
            setRealmStale();
            assertRealmExistsAfterMS(context, async, 325);
        });
    }

    private void assertRealmExistsAfterMS(TestContext context, Async async, int ms) {
        mock.vertx().setTimer(ms, handler -> {
            Future<RealmSettings> future = Future.future();

            future.setHandler(event -> {
                context.assertTrue(event.succeeded());
                context.assertNotNull(event.result());
                async.complete();
            });

            realms.get(future, REALM_NAME);
        });
    }

    private static class WritableContextMock extends ContextMock {
        public int timeout = STALE_TIMEOUT;

        @Override
        public int realmTimeout() {
            return timeout;
        }
    }
}
