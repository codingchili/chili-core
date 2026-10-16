package com.codingchili.core.storage;

import com.codingchili.core.testing.StorageObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.googlecode.cqengine.query.QueryFactory.equal;

/**
 * Tests for the CQEngine indexed storage.
 */
@RunWith(VertxUnitRunner.class)
public class IndexedMapPersistedTest extends MapTestCases {

    @Before
    public void setUp(TestContext test) {
        super.setUp(test, IndexedMapPersisted.class);
    }

    @After
    public void tearDown(TestContext test) {
        super.tearDown(test);
    }

    private IndexedMapPersisted<StorageObject> map() {
        return (IndexedMapPersisted<StorageObject>) store;
    }

    private int versions(String id) {
        try (var result = map().getDatabase().retrieve(equal(map().FIELD_ID, id))) {
            return result.size();
        }
    }

    @Test
    public void heal_removesDuplicateVersions(TestContext test) {
        map().getDatabase().add(new StorageObject("dup", 1));
        map().getDatabase().add(new StorageObject("dup", 2));
        test.assertEquals(2, versions("dup"));

        map().heal();

        test.assertEquals(1, versions("dup"));
        store.get("dup").onComplete(test.asyncAssertSuccess());
    }

    @Test
    public void put_replacesAllVersions(TestContext test) {
        map().getDatabase().add(new StorageObject("dup", 1));
        map().getDatabase().add(new StorageObject("dup", 2));

        store.put(new StorageObject("dup", 3)).onComplete(test.asyncAssertSuccess(done -> {
            test.assertEquals(1, versions("dup"));
            store.remove("dup").onComplete(test.asyncAssertSuccess(removed -> {
                test.assertEquals(0, versions("dup"));
            }));
        }));
    }
}
