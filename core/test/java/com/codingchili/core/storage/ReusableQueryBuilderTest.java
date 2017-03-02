package com.codingchili.core.storage;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Robin Duda
 *         <p>
 *         Tests for the reusable-query builder.
 */
@RunWith(VertxUnitRunner.class)
public class ReusableQueryBuilderTest {

    @Test
    public void testOutputMatchesJson(TestContext test) {
        JsonObject query = new ReusableQueryBuilder<>("name").like("robin")
                .or("name").like("anjah")
                .or("level").between(99L, 101L)
                .and("class").matches("Paladin.*")
                .and("skill").in("woodcutting", "cooking").equalTo(100)
                .orderBy("strength").order(SortOrder.ASCENDING)
                .page(2).pageSize(32)
                .compile();

        JsonObject verification = new JsonObject("{\"query\":" +
                "[" +
                "[{\"name\":{\"like\":\"robin\"}}]," +  // and
                "[{\"name\":{\"like\":\"anjah\"}}]," +
                "[" +                                   // or
                "{\"level\":{\"between\":{\"minimum\":99,\"maximum\":101}}}," + //and
                "{\"class\":{\"matches\":\"Paladin.*\"}}," +
                "{\"skill\":{\"in\":[\"woodcutting\",\"cooking\"]}}," +
                "{\"skill\":{\"equalTo\":100}}" +
                "]" +
                "],\"options\":{\"orderBy\":\"strength\",\"order\":\"ASCENDING\",\"page\":2,\"pageSize\":32}}");

        test.assertEquals(verification.encodePrettily(), query.encodePrettily());
        System.out.println(query.encodePrettily());
    }

}
