package com.codingchili.core.storage;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.storage.exception.NothingToRemoveException;
import com.codingchili.core.storage.exception.NothingToReplaceException;
import com.codingchili.core.storage.exception.ValueAlreadyPresentException;
import com.codingchili.core.storage.exception.ValueMissingException;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.MultiValueAttribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.index.navigable.NavigableIndex;
import com.googlecode.cqengine.index.radix.RadixTreeIndex;
import com.googlecode.cqengine.index.suffix.SuffixTreeIndex;
import com.googlecode.cqengine.index.unique.UniqueIndex;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.QueryFactory;
import com.googlecode.cqengine.query.option.AttributeOrder;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.resultset.ResultSet;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.WorkerExecutor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.googlecode.cqengine.query.QueryFactory.*;
import static com.googlecode.cqengine.query.option.DeduplicationStrategy.LOGICAL_ELIMINATION;
import static io.vertx.core.Future.failedFuture;
import static io.vertx.core.Future.succeededFuture;

/**
 * @author Robin Duda
 * <p>
 * A storage implementation that is local and indexed. Always use this when using queries.
 * The indexing is fully based on CQEngine. see http://github.com/npgall/cqengine
 * The db/collection is shared over multiple instances.
 */
public abstract class IndexedMap<Value extends Storable> implements AsyncStorage<Value> {
    private static final Map<String, SharedIndexCollection> maps = new HashMap<>();
    private final Map<String, Attribute<Value, String>> fields = new HashMap<>();
    private final SimpleAttribute<Value, String> FIELD_ID;
    private final StorageContext<Value> context;
    private final WorkerExecutor executor;
    private SharedIndexCollection<Value> db;

    @SuppressWarnings("unchecked")
    public IndexedMap(Future<AsyncStorage<Value>> future, StorageContext<Value> context) {
        this.context = context;
        executor = context.vertx().createSharedWorkerExecutor("IndexedMap", 1);
        FIELD_ID = attribute(context.clazz(), String.class, Storable.idField, Storable::id);
        fields.put(Storable.idField, FIELD_ID);

        // share collections that share the same identifier.
        synchronized (maps) {
            if (maps.containsKey(context.identifier())) {
                db = maps.get(context.identifier());
            } else {
                db = getImplementation(context, FIELD_ID);
                db.addIndex(UniqueIndex.onAttribute(FIELD_ID));
                maps.put(context.identifier(), db);
            }
        }
        future.complete(this);
    }

    /**
     * Allows parameterization of the indexed collection.
     *
     * @param ctx       the storage context used.
     * @param attribute the primary attribute of the Storable
     * @return a configured IndexedCollection with persistence etc configured.
     */
    protected abstract SharedIndexCollection<Value> getImplementation(
            StorageContext<Value> ctx, SimpleAttribute<Value, String> attribute);

    @Override
    public void get(String key, Handler<AsyncResult<Value>> handler) {
        Iterator<Value> result = db.retrieve(equal(FIELD_ID, key)).iterator();

        if (result.hasNext()) {
            handler.handle(succeededFuture(result.next()));
        } else {
            handler.handle(failedFuture(new ValueMissingException(key)));
        }
    }

    @Override
    public void put(Value value, Handler<AsyncResult<Void>> handler) {
        executor.executeBlocking(blocking -> get(value.id(), get -> {
            if (get.succeeded()) {
                db.remove(get.result());
            }
            db.add(value);
            blocking.complete();
        }), handler);
    }

    @Override
    public void putIfAbsent(Value value, Handler<AsyncResult<Void>> handler) {
        if (!db.contains(value)) {
            put(value, handler);
        } else {
            handler.handle(failedFuture(new ValueAlreadyPresentException(value.id())));
        }
    }

    @Override
    public void remove(String key, Handler<AsyncResult<Void>> handler) {
        get(key, got -> {
            if (got.succeeded() && db.remove(got.result())) {
                handler.handle(succeededFuture());
            } else {
                handler.handle(failedFuture(new NothingToRemoveException(key)));
            }
        });
    }

    @Override
    public void update(Value value, Handler<AsyncResult<Void>> handler) {
        remove(value.id(), removed -> {
            if (removed.succeeded()) {
                put(value, handler);
            } else {
                handler.handle(failedFuture(new NothingToReplaceException(value.id())));
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public void values(Handler<AsyncResult<Collection<Value>>> handler) {
        handler.handle(succeededFuture(db));
    }

    @Override
    public void clear(Handler<AsyncResult<Void>> handler) {
        db.clear();
        handler.handle(succeededFuture());
    }

    @Override
    public void size(Handler<AsyncResult<Integer>> handler) {
        handler.handle(succeededFuture(db.size()));
    }

    /**
     * Adds an index for the given attribute if one is not already added.
     * hack CQEngine to avoid having to specify attributes. (serialize to json, then get field value.)
     *
     * @param fieldName the field to add indexing to.
     */
    @SuppressWarnings("unchecked")
    private Attribute<Value, String> createIndex(String fieldName, boolean multiValue) {
        Attribute<Value, String> attribute;

        if (!fields.containsKey(fieldName)) {
            if (multiValue) {
                attribute = new MultiValueAttribute<Value, String>((Class<Value>) Generic.class, String.class, fieldName) {
                    @Override
                    public Iterable<String> getValues(Value indexing, QueryOptions queryOptions) {
                        return Arrays.stream(Serializer.getValueByPath(context.toJson(indexing), fieldName))
                                .map(item -> (item + ""))::iterator;
                    }
                };
            } else {
                attribute = attribute(fieldName, (indexing) -> {
                    return (Serializer.getValueByPath(context.toJson(indexing), fieldName)[0] + "");
                });
            }
            fields.put(fieldName, attribute);

            synchronized (maps) {
                if (!db.isIndexed(fieldName)) {
                    db.setIndexed(fieldName);
                    db.addIndex(NavigableIndex.onAttribute(attribute));
                    db.addIndex(SuffixTreeIndex.onAttribute(attribute));
                    db.addIndex(RadixTreeIndex.onAttribute(attribute));
                }
            }
        }
        return fields.get(fieldName);
    }

    @Override
    public QueryBuilder<Value> query(String attribute) {
        return new AbstractQueryBuilder<Value>(this, attribute) {
            Attribute<Value, String> field = fields.get(attribute);
            List<Query<Value>> statements = new ArrayList<>();
            Query<Value> builder;

            @Override
            public QueryBuilder<Value> and(String attribute) {
                prepareField(attribute);
                return this;
            }

            @Override
            public QueryBuilder<Value> or(String attribute) {
                next();
                prepareField(attribute);
                return this;
            }

            private QueryBuilder<Value> prepareField(String attribute) {
                setAttribute(attribute);
                field = createIndex(attribute(), isAttributeArray());
                return this;
            }

            private void next() {
                Query<Value> bucket;

                if (statements.size() >= 2) {
                    bucket = QueryFactory.and(statements.remove(0), statements.remove(0), statements);
                } else if (statements.size() == 1) {
                    bucket = statements.get(0);
                } else {
                    return; // empty statement, ignore.
                }

                if (builder == null) {
                    builder = bucket;
                } else {
                    builder = QueryFactory.or(builder, bucket);
                }
                statements.clear();
            }

            @Override
            public QueryBuilder<Value> between(Long minimum, Long maximum) {
                statements.add(QueryFactory.between(field, minimum + "", maximum + ""));
                return this;
            }

            @Override
            public QueryBuilder<Value> like(String text) {
                statements.add(QueryFactory.contains(field, text));
                return this;
            }

            @Override
            public QueryBuilder<Value> startsWith(String text) {
                statements.add(QueryFactory.startsWith(field, text));
                return this;
            }

            @Override
            public QueryBuilder<Value> in(Comparable... list) {
                statements.add(QueryFactory.in(field,
                        Arrays.stream(list)
                                .map(Object::toString)
                                .collect(Collectors.toList())));
                return this;
            }

            @Override
            public QueryBuilder<Value> equalTo(Comparable match) {
                statements.add(QueryFactory.equal(field, (match + "")));
                return this;
            }

            @Override
            public QueryBuilder<Value> matches(String regex) {
                statements.add(QueryFactory.matchesRegex(field, regex));
                return this;
            }

            @Override
            public void execute(Handler<AsyncResult<Collection<Value>>> handler) {
                next();

                context.blocking(blocking -> {
                    ResultSet<Value> values = db.retrieve(builder, getQueryOptions());
                    blocking.complete(StreamSupport.stream(values.spliterator(), false)
                            .skip(pageSize * page)
                            .limit(pageSize)
                            .collect(Collectors.toList()));
                    values.close();
                }, handler);
            }

            private QueryOptions getQueryOptions() {
                if (isOrdered) {
                    createIndex(getOrderByAttribute(), false);
                    AttributeOrder<Value> order;

                    if (sortOrder.equals(SortOrder.ASCENDING)) {
                        order = ascending(missingLast(fields.get(getOrderByAttribute())));
                    } else {
                        order = descending(missingLast(fields.get(getOrderByAttribute())));
                    }
                    return queryOptions(
                            QueryFactory.orderBy(order),
                            deduplicate(LOGICAL_ELIMINATION));
                } else {
                    return queryOptions(QueryFactory.deduplicate(LOGICAL_ELIMINATION));
                }
            }
        }.prepareField(attribute);
    }

    @Override
    public StorageContext<Value> context() {
        return context;
    }

    /**
     * when creating an index on a multivalued attribute a reflective operation is invoked.
     * This reflective invocation fails since Value is of generic type. To circumvent this,
     * a class that implements Storable, which is the common interface with Value is used.
     */
    private abstract class Generic implements Storable {
    }
}
