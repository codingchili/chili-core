package com.codingchili.core.storage;

import com.codingchili.core.context.CoreRuntimeException;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests the standalone query builder.
 */
@RunWith(VertxUnitRunner.class)
public class QueryTest {

    @Test
    public void testGenerateQueryString(TestContext test) {
        String query = Query.on("cat.type")
                .in("siamese", "perser", "ragdoll")
                .and("cat.color").equalTo("white")
                .or("cat.lifestyle").in("amphibians", "wateranimal").matches("[water].*")
                .or("cat.age").between(0L, 100L).and("cat.name").startsWith("fl")
                .orderBy("cat.name").order(SortOrder.ASCENDING)
                .page(3).pageSize(24)
                .setName("findCatsQ")
                .toString();
        System.out.println(query);
    }


    @Test
    public void testParseQueryString() {

    }

    @Test
    public void testQueryWithStorage() {

    }

    @Test
    public void testQueryWithPoll() {

    }

    @Test
    public void testErrorWhenStorageNotSet(TestContext test) {
        try {
            Query.on("x").execute((done -> { }));
            test.fail("did not throw exception when storage null");
        } catch (CoreRuntimeException e) {
        }
        try {
            Query.on("x").poll((storable) -> { }, () -> 0);
            test.fail("did not throw exception when storage null");
        } catch (CoreRuntimeException e) {
        }
    }

    @Test
    public void testMapperCalled() {

    }
}