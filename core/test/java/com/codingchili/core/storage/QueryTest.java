package com.codingchili.core.storage;

import com.codingchili.core.context.*;
import com.codingchili.core.security.Account;

import io.vertx.core.Future;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

import static com.codingchili.core.storage.QueryParser.next;

/**
 * Tests the standalone query builder.
 */
@RunWith(VertxUnitRunner.class)
public class QueryTest {
    private static StorageContext<Account> context;
    private QueryParser<Account> parser;

    @BeforeClass
    public static void setUpContext() {
        context = new StorageContext<Account>()
                .setDatabase("")
                .setCollection("")
                .setClass(Account.class)
                .setPlugin(JsonMap.class);
    }

    @AfterClass
    public static void tearDown(TestContext test) {
        context.close(test.asyncAssertSuccess());
    }

    @Before
    public void setUp() {
        parser = new QueryParser<>(new JsonMap<>(Future.future(), context)::query);
    }

    @Test
    public void testGenerateQueryString(TestContext test) {
        String query = new Query<>().on("cat.type")
                .in("siamese", "perser", "ragdoll")
                .and("cat.color").equalTo("white")
                .or("cat.lifestyle").in("amphibians", "wateranimal").matches("[water].*")
                .or("cat.age").between(0L, 100L).and("cat.name").startsWith("fl")
                .orderBy("cat.name").order(SortOrder.ASCENDING)
                .page(3).pageSize(24)
                .setName("findCatsQ")
                .toString();
    }

    @Test
    public void testParseQueryString(TestContext test) {
        QueryBuilder<Account> builder = new Query<>();
        QueryParser<Account> parser = new QueryParser<>(() -> builder);

        parser.parse("NAMED QUERY 'findCats Query' ON\n" +
                "    cat.type IN (siamese,perser,ragdoll) AND cat.color EQ white \n" +
                "\tOR cat.lifestyle IN (amphibians,wateranimal) AND cat.address REGEX([water ].*) \n" +
                "\tOR cat.age BETWEEN 0 100 AND cat.name STARTSWITH fl \n" +
                "ORDERBY cat.name ASCENDING PAGE 3 PAGESIZE 24\n");

        System.out.println(builder.toString());
    }

    @Test
    public void testCallCustomFunction(TestContext test) {
        parser.customize().put("success", (builder, matcher) -> {
            test.assertEquals(Boolean.TRUE.toString(), QueryParser.nextValue(matcher));
        });
        parser.parse("success(true)");
    }

    @Test
    public void testCallGlobalFunction(TestContext test) {
        QueryParser.defaults().put("global", (builder, matcher) -> {
            test.assertEquals(Boolean.TRUE.toString(), QueryParser.nextValue(matcher));
        });
        parser.parse("global(true)");
    }

    @Test
    public void testInferDataType(TestContext test) {
        test.assertEquals(QueryParser.toComparable("true").getClass(), Boolean.class);
        test.assertEquals(QueryParser.toComparable("100").getClass(), Integer.class);
        test.assertEquals(QueryParser.toComparable("5.5").getClass(), Double.class);
        test.assertEquals(QueryParser.toComparable("5,5").getClass(), Double.class);
        test.assertEquals(QueryParser.toComparable("a string").getClass(), String.class);
    }

    @Test
    public void testErrorWhenStorageNotSet(TestContext test) {
        try {
            new Query<>().on("x").execute((done -> {
            }));
            test.fail("did not throw exception when storage null");
        } catch (CoreRuntimeException e) {
        }
        try {
            new Query<>().on("x").poll((storable) -> {
            }, TimerSource.of(0));
            test.fail("did not throw exception when storage null");
        } catch (CoreRuntimeException e) {
        }
    }
}