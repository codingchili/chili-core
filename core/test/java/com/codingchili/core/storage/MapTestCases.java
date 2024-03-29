package com.codingchili.core.storage;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.codingchili.core.context.*;
import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.storage.exception.*;
import com.codingchili.core.testing.StorageObject;

import static com.codingchili.core.configuration.CoreStrings.ID_NAME;
import static com.codingchili.core.logging.Level.WARNING;


/**
 * Common test cases for the map implementations.
 */
@Ignore("Extend this class to run the tests.")
@RunWith(VertxUnitRunner.class)
public class MapTestCases {
    protected static final String COLLECTION = "MapTestCases";
    protected Long TEST_ITEM_COUNT = 200L;
    private static final Long SNOWFLAKE_INTERVAL = 10L;
    private final Long SNOWFLAKE_COUNT = TEST_ITEM_COUNT / SNOWFLAKE_INTERVAL;
    private static final Long SNOWFLAKE_BASE_LEVEL = 9000L;
    private final Long SNOWFLAKE_MAX_LEVEL = SNOWFLAKE_BASE_LEVEL + SNOWFLAKE_COUNT;
    private static final String LEVEL = "level";
    private static final String NAME = "name";
    private static final String ONE = "id.1";
    private static final String TWO = "id.2";
    private static final String SNOWFLAKE_NAME_PREFIX = "oneSpecialSnowflake";
    private static final String NAME_MISSING = "id.missing";
    private static final String REGEX_ALL = ".*";
    private static final StorageObject OBJECT_ONE = new StorageObject(ONE, 1);
    private static final StorageObject OBJECT_TWO = new StorageObject(TWO, 2);
    private static final String SNOW_KEYWORD = "SNOW";
    private static final int LEVEL_BUCKET_SIZE = 10;
    protected static Integer STARTUP_DELAY = 1;

    static {
        AttributeRegistry.register(db -> {
            db.single(js -> js.getString("name"), String.class, "name");
            db.single(js -> js.getInteger("level"), Integer.class, "level");
        }, JsonObject.class);
    }

    @Rule
    public Timeout timeout = Timeout.seconds(60);

    protected Class<? extends AsyncStorage> plugin;
    protected StorageContext<StorageObject> context;
    protected AsyncStorage<StorageObject> store;

    protected void setUp(TestContext test, Class<? extends AsyncStorage> plugin) {
        setUp(test, plugin, new SystemContext());
    }

    @After
    public void tearDown(TestContext test) {
        context.close(test.asyncAssertSuccess());
    }

    protected void setUp(TestContext test, Class<? extends AsyncStorage> plugin, CoreContext context) {
        this.context = new StorageContext<>(context);
        this.plugin = plugin;
        Async async = test.async();

        new StorageLoader<StorageObject>(context)
                .withDB(plugin.getSimpleName(), COLLECTION)
                .withValue(StorageObject.class)
                .withPlugin(plugin)
                .build(result -> {
                    if (result.succeeded()) {
                        store = result.result();
                        prepareStore(async);
                    } else {
                        test.fail(result.cause());
                    }
                });
    }

    private void prepareStore(Async async) {
        store.clear(clear -> {
            Assert.assertTrue(clear.succeeded());
            AtomicInteger inserted = new AtomicInteger(0);

            context.periodic(TimerSource.of(50).setName("startup timer"), handler -> {
                if (inserted.get() == TEST_ITEM_COUNT) {
                    context.timer(STARTUP_DELAY, event -> async.complete());
                    context.cancel(handler);
                }
            });
            for (long i = 0L; i < TEST_ITEM_COUNT; i++) {
                StorageObject object = new StorageObject("id." + i, Long.valueOf(i % LEVEL_BUCKET_SIZE).intValue());

                if (i % SNOWFLAKE_INTERVAL == 0) {
                    object.setName(SNOWFLAKE_NAME_PREFIX + i);
                    object.setLevel(Long.valueOf(9000L + (i / SNOWFLAKE_INTERVAL)).intValue());
                    object.getKeywords().add(SNOW_KEYWORD);
                }

                store.put(object, done -> {
                    if (done.failed()) {
                        throw new RuntimeException(done.cause());
                    }
                    Assert.assertTrue(errorText(done), done.succeeded());
                    inserted.incrementAndGet();
                });
            }
        });
    }

    protected String errorText(AsyncResult result) {
        if (result != null) {
            if (result.cause() != null) {
                result.cause().printStackTrace();
            }
            return (result.cause() != null) ? result.cause().getMessage() : null;
        } else {
            return "";
        }
    }

    @Test
    public void testGet(TestContext test) {
        Async async = test.async();

        store.get(TWO, get -> {
            test.assertTrue(get.succeeded());
            test.assertEquals(OBJECT_TWO, get.result());
            async.complete();
        });
    }

    @Test
    public void testGetMissing(TestContext test) {
        Async async = test.async();

        store.get(NAME_MISSING, get -> {
            test.assertTrue(get.failed());
            test.assertNull(get.result());
            test.assertEquals(ValueMissingException.class, get.cause().getClass());
            async.complete();
        });
    }

    @Test
    public void testPut(TestContext test) {
        Async async = test.async();

        store.put(OBJECT_ONE, put -> {
            test.assertTrue(put.succeeded());

            store.size(size -> {
                test.assertEquals(TEST_ITEM_COUNT.intValue(), size.result());
                store.get(ONE, get -> {
                    test.assertEquals(OBJECT_ONE, get.result());
                    test.assertTrue(get.succeeded());
                    async.complete();
                });
            });
        });
    }

    @Test
    public void testContainsKey(TestContext test) {
        Async async = test.async();

        store.contains(TWO, contains -> {
            test.assertTrue(contains.result());

            store.contains(UUID.randomUUID().toString(), done -> {
                test.assertFalse(done.result());
                async.complete();
            });
        });
    }

    @Test
    public void testPutIfAbsent(TestContext test) {
        Async async = test.async();
        StorageObject missing = new StorageObject(NAME_MISSING, 0);

        store.putIfAbsent(missing, put -> {
            test.assertTrue(put.succeeded(), errorText(put));

            store.get(ONE, get -> {
                test.assertTrue(get.succeeded());
                async.complete();
            });
        });
    }

    @Test
    public void testPutIfAbsentNotAbsent(TestContext test) {
        Async async = test.async();

        store.putIfAbsent(OBJECT_TWO, put -> {
            test.assertTrue(put.failed());
            test.assertEquals(ValueAlreadyPresentException.class, put.cause().getClass());
            async.complete();
        });
    }

    @Test
    public void testRemove(TestContext test) {
        Async async = test.async();

        store.remove(TWO, remove -> {
            test.assertTrue(remove.succeeded());

            store.get(TWO, query -> {
                test.assertTrue(query.failed());
                async.complete();
            });
        });
    }

    @Test
    public void testRemoveNotPresent(TestContext test) {
        Async async = test.async();

        store.remove(NAME_MISSING, remove -> {
            test.assertTrue(remove.failed());
            test.assertEquals(NothingToRemoveException.class, remove.cause().getClass());
            async.complete();
        });
    }

    @Test
    public void testUpdate(TestContext test) {
        Async async = test.async();

        store.get(TWO, get -> {
            test.assertTrue(get.succeeded());
            StorageObject object = get.result();
            object.setLevel(1000);

            store.update(object, done -> {
                test.assertTrue(done.succeeded());

                store.get(TWO, updated -> {
                    test.assertTrue(updated.succeeded());
                    test.assertEquals(updated.result().getLevel(), 1000);

                    store.size(size -> {
                        test.assertEquals(TEST_ITEM_COUNT.intValue(), size.result());
                        async.complete();
                    });
                });

            });
        });
    }

    @Test
    public void testUpdateIfNotePresent(TestContext test) {
        Async async = test.async();
        StorageObject object = new StorageObject(NAME_MISSING, 0);

        store.update(object, replace -> {
            test.assertTrue(replace.failed());
            test.assertEquals(NothingToUpdateException.class, replace.cause().getClass());
            async.complete();
        });
    }

    @Test
    public void testClear(TestContext test) {
        Async async = test.async();

        store.clear(clear -> {
            test.assertTrue(clear.succeeded(), errorText(clear));

            store.size(size -> {
                test.assertEquals(0, size.result());
                test.assertTrue(size.succeeded());
                async.complete();
            });
        });
    }

    @Test
    public void testSize(TestContext test) {
        Async async = test.async();

        store.size(size -> {
            test.assertTrue(size.succeeded(), errorText(size));
            test.assertEquals(TEST_ITEM_COUNT.intValue(), size.result());
            async.complete();
        });
    }

    @Test
    public void testQueryMatchNone(TestContext test) {
        Async async = test.async();

        store.query(NAME).equalTo(NAME_MISSING).execute(query -> {
            test.assertTrue(query.succeeded(), errorText(query));
            test.assertEquals(0, query.result().size());
            async.complete();
        });
    }

    @Test
    public void testQueryLike(TestContext test) {
        Async async = test.async();

        store.query(NAME).like("Snowflake").execute(query -> {
            test.assertTrue(query.succeeded(), errorText(query));
            test.assertEquals(SNOWFLAKE_COUNT.intValue(), query.result().size());

            store.query(NAME).like("flake0").execute(inner -> {
                test.assertTrue(inner.succeeded());
                test.assertEquals(1, inner.result().size());
                async.complete();
            });
        });
    }

    @Test
    public void testRegexQuery(TestContext test) {
        Async async = test.async();

        store.query(NAME).matches(".*flake[0]").execute(query -> {
            test.assertTrue(query.succeeded(), errorText(query));
            test.assertEquals(1, query.result().size());
            test.assertTrue(query.result().iterator().next().getId().contains("flake0"));
            async.complete();
        });
    }

    @Test
    public void testQueryRange(TestContext test) {
        Async async = test.async();

        store.query(LEVEL).between(SNOWFLAKE_BASE_LEVEL, SNOWFLAKE_MAX_LEVEL).execute(query -> {
            test.assertTrue(query.succeeded(), errorText(query));
            test.assertEquals(SNOWFLAKE_COUNT.intValue(), query.result().size());

            query.result().stream().forEach(item -> {
                test.assertInRange(SNOWFLAKE_BASE_LEVEL,
                        item.getLevel(),
                        SNOWFLAKE_MAX_LEVEL - SNOWFLAKE_BASE_LEVEL);
            });

            async.complete();
        });
    }

    @Test
    public void testQueryRangeNoMatches(TestContext test) {
        Async async = test.async();

        store.query(LEVEL).between(-10L, -5L).execute(query -> {
            test.assertTrue(query.succeeded(), errorText(query));
            test.assertEquals(0, query.result().size());
            async.complete();
        });
    }

    @Test
    public void testLimitResults(TestContext test) {
        Async async = test.async();
        int pageSize = 4;

        store.query(NAME).matches(REGEX_ALL).pageSize(pageSize).execute(query -> {
            test.assertTrue(query.succeeded(), errorText(query));
            test.assertEquals(pageSize, query.result().size());
            async.complete();
        });
    }

    @Test
    public void testOrderResultsAscending(TestContext test) {
        Async async = test.async();

        findWithSort((find) -> {
            test.assertTrue(find.succeeded());
            test.assertNotEquals(0, find.result().size());
            test.assertTrue(sortedByLevel(find.result(), SortOrder.ASCENDING));
            async.complete();
        }, SortOrder.ASCENDING, test);
    }

    @Test
    public void testOrderResultsDescending(TestContext test) {
        Async async = test.async();

        findWithSort((find) -> {
            test.assertTrue(find.succeeded());
            test.assertNotEquals(0, find.result().size());
            test.assertTrue(sortedByLevel(find.result(), SortOrder.DESCENDING));
            async.complete();
        }, SortOrder.DESCENDING, test);
    }

    private void findWithSort(Handler<AsyncResult<Collection<StorageObject>>> handler, SortOrder mode, TestContext test) {
        int pageSize = 10;

        store.query(NAME)
                .matches(REGEX_ALL)
                .pageSize(pageSize)
                .orderBy(LEVEL)
                .order(mode)
                .execute(query -> {
                    test.assertTrue(query.succeeded(), errorText(query));
                    test.assertEquals(pageSize, query.result().size());
                    handler.handle(Future.succeededFuture(query.result()));
                });
    }

    private boolean sortedByLevel(Collection<StorageObject> items, SortOrder order) {
        int lastLevel = (items.size() != 0) ? items.iterator().next().getLevel() : 0;

        for (StorageObject item : items) {
            if (item.getLevel() < lastLevel && order.equals(SortOrder.ASCENDING) ||
                    item.getLevel() > lastLevel && order.equals(SortOrder.DESCENDING)) {
                new ConsoleLogger(getClass())
                        .log("Sort verification error!", WARNING)
                        .log("Last level was " + lastLevel + " using sortmode " + order.name(), WARNING)
                        .log(Serializer.json(item).encodePrettily(), WARNING);
                return false;
            }
            lastLevel = item.getLevel();
        }
        return true;
    }

    @Test
    public void testResultsAreDistinct(TestContext test) {
        Async async = test.async();

        store.query(NAME)
                .matches(REGEX_ALL)
                .or(NAME)
                .matches(REGEX_ALL)
                .pageSize(TEST_ITEM_COUNT.intValue())
                .execute(query -> {
                    test.assertTrue(query.succeeded(), errorText(query));
                    test.assertEquals(TEST_ITEM_COUNT.intValue(), query.result().size());
                    async.complete();
                });
    }

    @Test
    public void testAndQuery(TestContext test) {
        Async async = test.async();

        store.query(LEVEL)
                .between(0L, SNOWFLAKE_BASE_LEVEL + 5)
                .and(NAME)
                .equalTo(ONE)
                .or(NAME)
                .equalTo(TWO)
                .execute(query -> {
                    test.assertTrue(query.succeeded(), errorText(query));
                    test.assertEquals(2, query.result().size());
                    async.complete();
                });
    }

    @Test
    public void testOrQuery(TestContext test) {
        Async async = test.async();

        store.query(LEVEL)
                .between(0L, 0L)
                .or(LEVEL)
                .between(5L, 5L)
                .execute(query -> {
                    test.assertTrue(query.succeeded(), errorText(query));
                    test.assertNotEquals(0, query.result().size());

                    for (StorageObject item : query.result()) {
                        test.assertTrue(item.getLevel() == 5 || item.getLevel() == 0);
                    }

                    async.complete();
                });
    }

    @Test
    public void testStartsWith(TestContext test) {
        Async async = test.async();

        store.query(NAME).startsWith(SNOWFLAKE_NAME_PREFIX)
                .execute(query -> {
                    test.assertTrue(query.succeeded(), errorText(query));
                    test.assertEquals(SNOWFLAKE_COUNT.intValue(), query.result().size());

                    for (StorageObject item : query.result()) {
                        test.assertTrue(item.getId().startsWith(SNOWFLAKE_NAME_PREFIX));
                    }
                    async.complete();
                });
    }

    @Test
    public void testMultipleRegexQueries(TestContext test) {
        Async async = test.async();

        store.query(NAME)
                .matches(".*e0")
                .and(NAME)
                .matches(SNOWFLAKE_NAME_PREFIX + ".*")
                .execute(query -> {
                    test.assertTrue(query.succeeded(), errorText(query));
                    test.assertNotEquals(0, query.result().size());

                    for (StorageObject item : query.result()) {
                        test.assertTrue(item.getId().matches(SNOWFLAKE_NAME_PREFIX + ".*[0-9]"));
                    }
                    async.complete();
                });
    }

    @Test
    public void testAttributeInList(TestContext test) {
        Async async = test.async();

        store.query(NAME)
                .in(ONE, TWO)
                .execute(query -> {
                    test.assertTrue(query.succeeded(), errorText(query));
                    test.assertEquals(2, query.result().size());

                    for (StorageObject item : query.result()) {
                        test.assertTrue(item.getId().equals(ONE) || item.getId().equals(TWO));
                    }
                    async.complete();
                });
    }

    @Test
    public void testPagingOffsetSorted(TestContext test) {
        Async async = test.async();

        store.query(NAME)
                .matches(REGEX_ALL)
                .page(3) // skip all level 0 and 1.
                .pageSize(LEVEL_BUCKET_SIZE)
                .orderBy(LEVEL)
                .order(SortOrder.ASCENDING)
                .execute(query -> {
                    test.assertTrue(query.succeeded(), errorText(query));
                    test.assertNotEquals(0, query.result().size());

                    for (StorageObject item : query.result()) {
                        test.assertEquals(2, item.getLevel());
                    }
                    async.complete();
                });
    }

    @Test
    public void testPagingWithOtherSize(TestContext test) {
        Async async = test.async();
        int pageSize = 12;

        store.query(NAME).matches(REGEX_ALL).pageSize(pageSize).execute(query -> {
            test.assertTrue(query.succeeded(), errorText(query));
            test.assertEquals(pageSize, query.result().size());
            async.complete();
        });
    }

    @Test
    public void testSuperSpecificQuery(TestContext test) {
        Async async = test.async();

        store.query("nested.name")
                .startsWith(StorageObject.NESTED_PREFIX)
                .or(NAME)
                .startsWith(SNOWFLAKE_NAME_PREFIX)
                .or(NAME)
                .startsWith(NAME)
                .pageSize(TEST_ITEM_COUNT.intValue())
                .execute(query -> {
                    test.assertTrue(query.succeeded(), errorText(query));
                    test.assertEquals(TEST_ITEM_COUNT.intValue(), query.result().size());
                    async.complete();
                });
    }

    @Test
    public void testSortByNestedField(TestContext test) {
        Async async = test.async();

        store.query("nested.name")
                .order(SortOrder.ASCENDING)
                .matches(REGEX_ALL)
                .execute(query -> {
                    test.assertTrue(query.succeeded(), errorText(query));
                    test.assertNotEquals(0, query.result().size());

                    String last = "";
                    for (StorageObject item : query.result()) {
                        test.assertTrue(last.compareTo(item.getNested().getName()) <= 0);
                        last = item.getNested().getName();
                    }
                    async.complete();
                });
    }

    @Test
    public void testQueryOnNestedField(TestContext test) {
        Async async = test.async();

        store.query("nested.name").matches(StorageObject.NESTED_PREFIX + REGEX_ALL).execute(query -> {
            test.assertTrue(query.succeeded(), errorText(query));
            test.assertNotEquals(0, query.result().size());

            for (StorageObject item : query.result()) {
                test.assertTrue(item.getNested().getName().startsWith(StorageObject.NESTED_PREFIX));
            }
            async.complete();
        });
    }

    @Test
    public void testCaseSensitivityEqualsNotIgnored(TestContext test) {
        Async async = test.async();

        store.query(NAME).equalTo(ONE.toUpperCase()).execute(query -> {
            test.assertTrue(query.succeeded(), errorText(query));
            test.assertEquals(0, query.result().size());
            async.complete();
        });
    }

    @Test
    public void testCaseSensitivityLikeIgnored(TestContext test) {
        Async async = test.async();

        store.query(NAME).like(SNOWFLAKE_NAME_PREFIX.substring(1, SNOWFLAKE_NAME_PREFIX.length() - 2))
                .execute(query -> {
                    test.assertTrue(query.succeeded(), errorText(query));
                    test.assertNotEquals(0, query.result().size());
                    async.complete();
                });
    }

    @Test
    public void testQueryWithUppercases(TestContext test) {
        Async async = test.async();
        String upper = "UPPERcase";
        StorageObject item = new StorageObject(upper, 1);
        store.put(item, done -> {
            test.assertTrue(done.succeeded());

            store.query(ID_NAME).equalTo(upper).execute(query -> {
                test.assertTrue(query.succeeded(), errorText(query));
                test.assertEquals(1, query.result().size());
                test.assertEquals(upper, query.result().iterator().next().getId());
                async.complete();
            });
        });
    }

    @Test
    public void testQueryFlatArray(TestContext test) {
        Async async = test.async();

        store.query("keywords[]").equalTo(SNOW_KEYWORD).execute(query -> {
            test.assertTrue(query.succeeded(), errorText(query));
            test.assertNotEquals(0, query.result().size());

            for (StorageObject item : query.result()) {
                test.assertTrue(item.getKeywords().contains(SNOW_KEYWORD));
            }
            async.complete();
        });
    }

    @Test
    public void testQueryNestedArray(TestContext test) {
        Async async = test.async();

        store.query("nested.numbers[]").in(7, 42).execute(query -> {
            test.assertTrue(query.succeeded(), errorText(query));
            test.assertNotEquals(0, query.result().size());

            for (StorageObject item : query.result()) {
                test.assertTrue(item.getNested().getNumbers().contains(7));
                test.assertTrue(item.getNested().getNumbers().contains(42));
            }
            async.complete();
        });
    }

    @Test
    public void testArrayIdentifierInSubstatement(TestContext test) {
        Async async = test.async();
        store.query("nested.numbers[]").between(10L, 42L)
                .and("nested.numbers[]").between(0L, 10L)
                .execute(query -> {
                    test.assertTrue(query.succeeded(), errorText(query));
                    test.assertNotEquals(0, query.result().size());
                    async.complete();
                });
    }

    @Test
    public void testGetValues(TestContext test) {
        Async async = test.async();
        store.values(result -> {
            test.assertTrue(result.succeeded());
            test.assertEquals((int) result.result().count(),
                    TEST_ITEM_COUNT.intValue());

            async.complete();
        });
    }

    @Test
    public void testFireQueryMultipleTimes(TestContext test) {
        Async async = test.async();
        int expectedHits = SNOWFLAKE_COUNT.intValue() / 2;

        QueryBuilder<StorageObject> builder = store
                .query(NAME).startsWith(SNOWFLAKE_NAME_PREFIX)
                .page(1)
                .pageSize(expectedHits); // important to verify that the pager is reset.

        builder.execute(query -> {
            test.assertTrue(query.succeeded());
            test.assertEquals(expectedHits, query.result().size());
            StorageObject first = query.result().iterator().next();

            builder.execute(inner -> {
                test.assertTrue(inner.succeeded());
                test.assertEquals(expectedHits, inner.result().size());
                test.assertEquals(first, inner.result().iterator().next());
                async.complete();
            });
        });
    }

    @Test
    public void testPollStorage(TestContext test) {
        Async async = test.async();
        AtomicInteger countdown = new AtomicInteger(2);

        store.query(NAME).startsWith(SNOWFLAKE_NAME_PREFIX).pageSize(1).poll(matches -> {
            if (countdown.decrementAndGet() == 0) {
                async.complete();
            }
        }, TimerSource.of(50));
    }

    @Test
    public void testAddIndexTwice() {
        store.addIndex("name");
        store.addIndex("name");
    }

    @Test
    public void testAddNestedIndex() {
        store.addIndex("nested.name");
    }

    @Test
    public void testAddArrayIndex() {
        store.addIndex("keywords[]");
    }

    @Test
    public void testStorageIsShared(TestContext test) {
        Async async = test.async();
        StorageContext context2 = new StorageContext<>(context);

        // creates a new storage using another context with the same DB/colletion
        new StorageLoader<StorageObject>(context2)
                .withDB(plugin.getSimpleName(), COLLECTION)
                .withValue(StorageObject.class)
                .withPlugin(plugin)
                .build(result -> {
                    AsyncStorage<StorageObject> newStorage = result.result();
                    store.size(size -> {
                        newStorage.size(newSize -> {
                            test.assertEquals(size.result(), newSize.result());
                            test.assertEquals(newSize.result(), TEST_ITEM_COUNT.intValue());
                            async.complete();
                        });
                    });
                });
    }
}
