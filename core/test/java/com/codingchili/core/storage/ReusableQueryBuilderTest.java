package com.codingchili.core.storage;

import com.codingchili.core.context.SystemContext;
import com.codingchili.core.testing.StorageObject;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Robin Duda
 *         <p>
 *         Tests for the reusable-query builder.
 */
@RunWith(VertxUnitRunner.class)
public class ReusableQueryBuilderTest {
    private static final String FIRSTNAME = "firstname";
    private static final String OTHERNAME = "othername";
    private static final String REGEXP = "regexp";
    private static final String NAME = "name";
    private static final String LEVEL = "level";
    private static final String ORDERING = "ordering";
    private static final Long LOW = 10L;
    private static final Long MEDIUM = 50L;
    private static final Long HIGH = 100L;
    public static final int PAGE = 0;
    public static final int PAGE_SIZE = 32;
    private AsyncStorage<StorageObject> storage;
    private Vertx vertx;

    @Before
    public void setUp(TestContext test) {
        Async async = test.async();
        vertx = Vertx.vertx();
        new StorageLoader<StorageObject>().jsonmap(new SystemContext(vertx))
                .withClass(StorageObject.class)
                .withDB(getClass().getName(), "")
                .build(result -> {
                    this.storage = result.result();
                    storage.put(new StorageObject(NAME, MEDIUM.intValue()), put -> {
                        async.complete();
                    });
                });
    }

    @After
    public void tearDown(TestContext test) {
        vertx.close(test.asyncAssertSuccess());
    }

    @Test
    public void testOutputMatchesJson(TestContext test) {
        JsonObject query = getAndQuery().serialize();
        System.out.println(query.encodePrettily());
    }

    private ReusableQueryBuilder<StorageObject> getAndQuery() {
        return new ReusableQueryBuilder<StorageObject>(NAME).like(FIRSTNAME)
                .and(NAME).like(OTHERNAME)
                .and(LEVEL).between(LOW, MEDIUM)
                .and(NAME).matches(REGEXP)
                .and(NAME).startsWith(FIRSTNAME)
                .and(NAME).equalTo(FIRSTNAME)
                .and(LEVEL).in(LOW.intValue(), MEDIUM.intValue(), HIGH.intValue())
                .and(LEVEL).equalTo(MEDIUM.intValue())
                .orderBy(ORDERING).order(SortOrder.ASCENDING)
                .page(PAGE).pageSize(PAGE_SIZE);
    }

    private ReusableQueryBuilder<StorageObject> getOrQuery() {
        return new ReusableQueryBuilder<StorageObject>(NAME).like(FIRSTNAME)
                .or(NAME).like(OTHERNAME)
                .or(LEVEL).between(LOW, MEDIUM)
                .or(NAME).matches(REGEXP)
                .or(NAME).startsWith(FIRSTNAME)
                .or(NAME).equalTo(FIRSTNAME)
                .or(LEVEL).in(LOW.intValue(), MEDIUM.intValue(), HIGH.intValue())
                .or(LEVEL).equalTo(MEDIUM.intValue())
                .orderBy(ORDERING).order(SortOrder.ASCENDING)
                .page(PAGE).pageSize(PAGE_SIZE);
    }

    @Ignore("Not implemented yet.")
    @Test
    public void testAndQuery(TestContext test) {
        //Async async = test.async();

        /*getAndQuery().execute(storage, handler -> {
            test.assertEquals(1, handler.result().size());
            async.complete();
        });*/
    }

    @Ignore("Not implemented yet.")
    @Test
    public void testOrQuery(TestContext test) {
        /*Async async = test.async();

        getOrQuery().execute(storage, handler -> {
            test.assertEquals(1, handler.result().size());
            async.complete();
        });*/
    }

    // todo test for broken json
    // todo test for invalid option
    // todo test for invalid query operator
    // todo test for empty query
    // todo add test for all query operators
    // todo add test for comparable values
    // todo add test for int,long,string,array
}
