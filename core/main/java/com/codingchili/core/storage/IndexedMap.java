package com.codingchili.core.storage;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.MultiValueAttribute;
import com.googlecode.cqengine.index.navigable.NavigableIndex;
import com.googlecode.cqengine.index.radix.RadixTreeIndex;
import com.googlecode.cqengine.index.suffix.SuffixTreeIndex;
import com.googlecode.cqengine.index.unique.UniqueIndex;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.QueryFactory;
import com.googlecode.cqengine.query.option.AttributeOrder;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.resultset.ResultSet;
import io.vertx.core.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.storage.exception.*;

import static com.googlecode.cqengine.query.QueryFactory.*;

/**
 * @author Robin Duda
 *         <p>
 *         A storage implementation that is local and indexed. Always use this when using queries.
 *         The indexing is fully based on CQEngine. see http://github.com/npgall/cqengine
 *         The db/collection is shared over multiple instances.
 */
public class IndexedMap<Value extends Storable> implements AsyncStorage<Value> {
    private Map<String, Attribute<Value, String>> fields = new ConcurrentHashMap<>();
    private IndexedCollection<Value> db = new ConcurrentIndexedCollection<>();
    private Attribute<Value, String> FIELD_ID;
    private StorageContext<Value> context;

    public IndexedMap(Future<AsyncStorage<Value>> future, StorageContext<Value> context) {
        this.context = context;
        FIELD_ID = attribute(Storable.idField, Storable::id);
        fields.put(Storable.idField, FIELD_ID);
        db.addIndex(UniqueIndex.onAttribute(FIELD_ID));
        future.complete(this);
    }

    @Override
    public void get(String key, Handler<AsyncResult<Value>> handler) {
        Iterator<Value> result = db.retrieve(equal(FIELD_ID, key)).iterator();

        if (result.hasNext()) {
            handler.handle(Future.succeededFuture(result.next()));
        } else {
            handler.handle(Future.failedFuture(new ValueMissingException(key)));
        }
    }

    @Override
    public void put(Value value, Handler<AsyncResult<Void>> handler) {
        db.update(Collections.singleton(value), Collections.singleton(value));
        handler.handle(Future.succeededFuture());
    }

    @Override
    public void put(Value value, long ttl, Handler<AsyncResult<Void>> handler) {
        put(value, handler);
        context.timer(ttl, event -> remove(value.id(), removed -> {
        }));
    }

    @Override
    public void putIfAbsent(Value value, Handler<AsyncResult<Void>> handler) {
        if (!db.contains(value)) {
            put(value, handler);
        } else {
            handler.handle(Future.failedFuture(new ValueAlreadyPresentException(value.id())));
        }
    }

    @Override
    public void putIfAbsent(Value value, long ttl, Handler<AsyncResult<Void>> handler) {
        putIfAbsent(value, handler);
        context.timer(ttl, event -> remove(value.id(), removed -> {
        }));
    }

    @Override
    public void remove(String key, Handler<AsyncResult<Void>> handler) {
        get(key, got -> {
            if (got.succeeded()) {
                db.update(Collections.singleton(got.result()), Collections.emptyList());
                handler.handle(Future.succeededFuture());
            } else {
                handler.handle(Future.failedFuture(new NothingToRemoveException(key)));
            }
        });
    }

    @Override
    public void update(Value value, Handler<AsyncResult<Void>> handler) {
        remove(value.id(), removed -> {
            if (removed.succeeded()) {
                put(value, handler);
            } else {
                handler.handle(Future.failedFuture(new NothingToReplaceException(value.id())));
            }
        });
    }

    @Override
    public void clear(Handler<AsyncResult<Void>> handler) {
        db.clear();
        handler.handle(Future.succeededFuture());
    }

    @Override
    public void size(Handler<AsyncResult<Integer>> handler) {
        handler.handle(Future.succeededFuture(db.size()));
    }

    /**
     * when creating an index on a multivalued attribute a reflective operation is invoked.
     * This reflective invocation fails since Value is of generic type. To circumvent this,
     * a class that implements Storable, which is the common interface with Value is used.
     */
    private abstract class Generic implements Storable {
    }

    /**
     * Adds an index for the given attribute if one is not already added.
     * hack CQEngine to avoid having to specify attributes. (serialize to json, then get field value.)
     *
     * @param fieldName the field to add indexing to.
     */
    private Attribute<Value, String> createIndex(String fieldName, boolean multiValue) {
        Attribute<Value, String> attribute = null;

        if (!fields.containsKey(fieldName)) {
            if (multiValue) {
                attribute = new MultiValueAttribute<Value, String>((Class<Value>) Generic.class, String.class, fieldName) {
                    @Override
                    public Iterable<String> getValues(Value indexing, QueryOptions queryOptions) {
                        return Arrays.asList(Serializer.getValueByPath(context.toJson(indexing), fieldName))
                                .stream().map(item -> item + "")::iterator;
                    }
                };
            } else {
                attribute = attribute(fieldName, (indexing) -> {
                    return (Serializer.getValueByPath(context.toJson(indexing), fieldName)[0] + "").toLowerCase();
                });
            }
            fields.put(fieldName, attribute);
            db.addIndex(NavigableIndex.onAttribute(attribute));
            db.addIndex(SuffixTreeIndex.onAttribute(attribute));
            db.addIndex(RadixTreeIndex.onAttribute(attribute));
        }
        return attribute;
    }

    @Override
    public QueryBuilder<Value> query(String attribute) {
        return new AbstractQueryBuilder<Value>(attribute) {
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
                createIndex(attribute(), isAttributeArray());
                field = fields.get(attribute());
                return this;
            }

            private void next() {
                Query<Value> bucket;

                if (statements.size() >= 2) {
                    bucket = QueryFactory.and(statements.remove(0), statements.remove(0), statements);
                } else if (statements.size() == 1) {
                    bucket = statements.remove(0);
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
                statements.add(QueryFactory.contains(field, text.toLowerCase()));
                return this;
            }

            @Override
            public QueryBuilder<Value> startsWith(String text) {
                statements.add(QueryFactory.startsWith(field, text.toLowerCase()));
                return this;
            }

            @Override
            public QueryBuilder<Value> in(Comparable... list) {
                statements.add(QueryFactory.in(field,
                        Arrays.asList(list).stream()
                                .map(Object::toString)
                                .collect(Collectors.toList())));
                return this;
            }

            @Override
            public QueryBuilder<Value> equalTo(Comparable match) {
                statements.add(QueryFactory.equal(field, match + ""));
                return this;
            }

            @Override
            public QueryBuilder<Value> matches(String regex) {
                statements.add(QueryFactory.matchesRegex(field, regex.toLowerCase()));
                return this;
            }

            @Override
            public void execute(Handler<AsyncResult<List<Value>>> handler) {
                next();

                context.blocking(blocking -> {
                    ResultSet<Value> values = db.retrieve(builder, getQueryOptions());
                    List<Value> list = StreamSupport.stream(values.spliterator(), false)
                            .skip(pageSize * page)
                            .limit(pageSize)
                            .collect(Collectors.toList());

                    handler.handle(Future.succeededFuture(list));
                }, false, handler);
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
                    return QueryFactory.queryOptions(QueryFactory.orderBy(order));
                } else {
                    return QueryFactory.noQueryOptions();
                }
            }

        }.prepareField(attribute);
    }
}
