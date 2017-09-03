package com.codingchili.core.storage;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.codingchili.core.testing.MapTestCases;
import com.codingchili.core.testing.StorageObject;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import static com.codingchili.core.configuration.CoreStrings.ID_NAME;

/**
 * @author Robin Duda
 *         <p>
 *         Tests for the CQEngine indexed storage.
 */
@RunWith(VertxUnitRunner.class)
public class IndexedMapTest extends MapTestCases {
    private static final String TEST_UPPER = "testUPPER";

    @Before
    public void setUp(TestContext test) {
        super.setUp(test, IndexedMapVolatile.class);
    }

    @After
    public void tearDown(TestContext test) {
        super.tearDown(test);
    }

    @Test
    public void testQueryWithUppercases(TestContext test) {
        Async async = test.async();
        StorageObject item = new StorageObject(TEST_UPPER, 1);
        store.put(item, done -> {
            test.assertTrue(done.succeeded());

            store.query(ID_NAME).equalTo(TEST_UPPER).execute(query -> {
                test.assertTrue(query.succeeded(), errorText(query));
                test.assertEquals(1, query.result().size());
                test.assertEquals(TEST_UPPER, query.result().iterator().next().id());
                async.complete();
            });
        });
    }

    @Ignore("Does not support mixing multiple modes of restricted character cases.")
    @Test
    public void testCaseSensitivityEqualsNotIgnored(TestContext test) {
    }
}
