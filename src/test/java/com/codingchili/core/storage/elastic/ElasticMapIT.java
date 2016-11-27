package com.codingchili.core.storage.elastic;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import com.codingchili.core.storage.ElasticMap;
import com.codingchili.core.testing.MapTestCases;

/**
 * @author Robin Duda
 *         <p>
 *         Tests for the storage providers in core. Reuse these tests when new
 *         storage subsystems are implemented using the StorageLoader.
 */
@Ignore
@RunWith(VertxUnitRunner.class)
public class ElasticMapIT extends MapTestCases {
    private static final int ELASTIC_REFRESH = 1200;

    @Before
    public void setUp(TestContext test) {
        super.setUp(test, ElasticMap.class);
    }

    @After
    public void tearDown(TestContext test) {
        super.tearDown(test);
    }

    /**
     * elasticsearch is near-realtime which means that size
     * does not return realtime results.
     */
    @Override
    @Test
    public void testSize(TestContext test) {
        Async async = test.async();

        store.put(KEY, VALUE, put -> {
            test.assertTrue(put.succeeded());

            context.timer(ELASTIC_REFRESH, event -> store.size(size -> {
                test.assertEquals(1, size.result());
                test.assertTrue(size.succeeded());
                async.complete();
            }));
        });
    }

    /**
     * elasticsearch is near-realtime which means that clear
     * does not return realtime results.
     */
    @Override
    @Test
    public void testClear(TestContext test) {
        Async async = test.async();

        store.put(KEY, VALUE, put -> {
            test.assertTrue(put.succeeded());

            store.clear(clear -> {
                test.assertTrue(clear.succeeded());

                context.timer(ELASTIC_REFRESH, event -> store.size(size -> {
                    test.assertEquals(0, size.result());
                    test.assertTrue(size.succeeded());
                    async.complete();
                }));
            });
        });
    }
}
