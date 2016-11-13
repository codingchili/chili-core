package com.codingchili.core.Storage;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
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
public class AsyncMapTest {
    private static final String VALUE = "value";
    private static final String VALUE_OTHER = "value_other";
    private static final String KEY = "key";
    private AsyncStorage<String, String> store;
    private CoreContext context;

    @Before
    public void setUp() {
        context = new ContextMock(Vertx.vertx());
        store = new AsyncLocalMap<>(context);
    }

    @After
    public void tearDown(TestContext test) {
        context.vertx().close(test.asyncAssertSuccess());
    }

    @Test
    public void testPut(TestContext test) {
        Async async = test.async();

        store.put(KEY, VALUE, handler -> store.get(KEY, get -> {
            test.assertEquals(VALUE, get.result());
            async.complete();
        }));
    }

    @Test
    public void testPutIfAbsent(TestContext test) {
        Async async = test.async();

        store.putIfAbsent(KEY, VALUE, put -> store.get(KEY, get -> {
            test.assertEquals(VALUE, get.result());
            async.complete();
        }));
    }

    @Test
    public void testPutIfAbsentNotAbsent(TestContext test) {
        Async async = test.async();

        store.putIfAbsent(KEY, VALUE, outer -> {
            test.assertNull(outer.result());

            store.putIfAbsent(KEY, VALUE_OTHER, inner -> {
                test.assertEquals(VALUE, inner.result());
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
                async.complete();
            });
        });
    }

    @Test
    public void testRemoveNonExisting(TestContext test) {
        Async async = test.async();

        store.put(KEY, VALUE, inner -> {
            test.assertEquals(null, inner.result());
            async.complete();
        });
    }

    @Test
    public void testRemoveIfPresent(TestContext test) {
        Async async = test.async();

        store.put(KEY, VALUE, outer -> {
            store.removeIfPresent(KEY, VALUE, inner -> {
                test.assertTrue(inner.result());
                async.complete();
            });
        });
    }

    @Test
    public void testRemoveIfPresentNotPresent(TestContext test) {
        Async async = test.async();

        store.removeIfPresent(KEY, VALUE, inner -> {
            test.assertFalse(inner.result());
            async.complete();
        });
    }

    @Test
    public void testRemoveIfAnotherValuePresent(TestContext test) {
        Async async = test.async();

        store.put(KEY, VALUE_OTHER, outer -> store.removeIfPresent(KEY, VALUE, inner -> {
            test.assertFalse(inner.result());
            async.complete();
        }));
    }

    @Test
    public void testReplace(TestContext test) {
        Async async = test.async();

        store.put(KEY, VALUE, outer -> store.replace(KEY, VALUE_OTHER, replace -> {
            test.assertEquals(VALUE, replace.result());
            async.complete();
        }));
    }

    @Test
    public void testReplaceIfPresent(TestContext test) {
        Async async = test.async();

        store.put(KEY, VALUE, outer -> store.replaceIfPresent(KEY, VALUE, VALUE_OTHER, replace -> {
            test.assertTrue(replace.result());
            async.complete();
        }));
    }

    @Test
    public void testReplaceIfNotPresent(TestContext test) {
        Async async = test.async();

        store.replaceIfPresent(KEY, VALUE, VALUE_OTHER, replace -> {
            test.assertFalse(replace.result());
            async.complete();
        });
    }

    @Test
    public void testReplaceIfAnotherValuePresent(TestContext test) {
        Async async = test.async();

        store.put(KEY, VALUE, outer -> store.replaceIfPresent(KEY, VALUE_OTHER, VALUE_OTHER, replace -> {
            test.assertFalse(replace.result());
            async.complete();
        }));
    }

    @Test
    public void testClear(TestContext test) {
        Async async = test.async();

        store.put(KEY, VALUE, outer -> store.clear(clear -> store.size(size -> {
            test.assertEquals(0, size.result());
            async.complete();
        })));
    }

    @Test
    public void testSize(TestContext test) {
        Async async = test.async();

        store.put(KEY, VALUE, put -> {
            store.size(size -> {
                test.assertEquals(1, size.result());
                async.complete();
            });
        });

    }
}
