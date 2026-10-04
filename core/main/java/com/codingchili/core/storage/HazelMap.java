package com.codingchili.core.storage;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.FutureHelper;
import com.codingchili.core.context.StorageContext;
import com.codingchili.core.storage.exception.NothingToRemoveException;
import com.codingchili.core.storage.exception.NothingToUpdateException;
import com.codingchili.core.storage.exception.ValueAlreadyPresentException;
import com.codingchili.core.storage.exception.ValueMissingException;
import com.hazelcast.config.IndexType;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.query.PagingPredicate;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.shareddata.AsyncMap;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static com.codingchili.core.configuration.CoreStrings.STORAGE_ARRAY;
import static com.codingchili.core.context.FutureHelper.error;
import static com.codingchili.core.context.FutureHelper.result;

/**
 * Initializes a new hazel async map.
 */
public class HazelMap<Value extends Storable> implements AsyncStorage<Value> {
    private static final String HAZEL_ARRAY = "[any]";
    private final Set<String> indexed = ConcurrentHashMap.newKeySet();
    private final StorageContext<Value> context;
    private AsyncMap<String, Value> map;
    private IMap<String, Value> imap;

    /**
     * Initializes a new hazel async map.
     *
     * @param context the context requesting the map to be created.
     * @param promise called when the map is created.
     */
    public HazelMap(Promise<AsyncStorage<Value>> promise, StorageContext<Value> context) {
        this.context = context;

        context.vertx().sharedData().<String, Value>getClusterWideMap(context.collection()).onComplete(cluster -> {
            if (cluster.succeeded()) {
                this.map = cluster.result();

                Optional<HazelcastInstance> hazel = Hazelcast.getAllHazelcastInstances().stream().findFirst();

                if (hazel.isPresent()) {
                    HazelcastInstance instance = hazel.get();
                    imap = instance.getMap(context.collection());
                    addIndex(Storable.idField);
                    promise.complete(this);
                } else {
                    promise.fail(CoreStrings.ERROR_NOT_CLUSTERED);
                }
            } else {
                promise.fail(cluster.cause());
            }
        });
    }

    @Override
    public Future<Value> get(String key) {
        return map.get(key).compose(value -> {
            if (value != null) {
                return result(value);
            } else {
                return error(new ValueMissingException(key));
            }
        });
    }

    @Override
    public Future<Void> put(Value value) {
        return map.put(value.getId(), value);
    }

    @Override
    public Future<Void> putIfAbsent(Value value) {
        return map.putIfAbsent(value.getId(), value).compose(previous -> {
            if (previous == null) {
                return FutureHelper.result();
            } else {
                return error(new ValueAlreadyPresentException(value.getId()));
            }
        });
    }

    @Override
    public Future<Void> remove(String key) {
        return map.remove(key).compose(removed -> {
            if (removed == null) {
                return error(new NothingToRemoveException(key));
            } else {
                return FutureHelper.result();
            }
        });
    }

    @Override
    public Future<Void> update(Value value) {
        return map.replace(value.getId(), value).compose(replaced -> {
            if (replaced == null) {
                return error(new NothingToUpdateException(value.getId()));
            } else {
                return FutureHelper.result();
            }
        });
    }

    @Override
    public Future<Stream<Value>> values() {
        return context.blocking(() -> imap.values().stream());
    }

    @Override
    public Future<Void> clear() {
        return map.clear();
    }

    @Override
    public Future<Integer> size() {
        return map.size();
    }

    @Override
    public void addIndex(String fieldName) {
        if (!indexed.contains(fieldName)) {
            indexed.add(fieldName);

            imap.addIndex(IndexType.SORTED, fieldName.replace(STORAGE_ARRAY, HAZEL_ARRAY));
        }
    }

    @Override
    public QueryBuilder<Value> query() {
        return new AbstractQueryBuilder<>(this, HAZEL_ARRAY) {
            private final List<Predicate<String, Value>> predicates = new ArrayList<>();
            private Predicate<String, Value> predicate;
            private BooleanOperator operator = BooleanOperator.AND;

            @Override
            public QueryBuilder<Value> on(String attribute) {
                setAttribute(attribute);
                return this;
            }

            @Override
            public QueryBuilder<Value> and(String attribute) {
                apply(BooleanOperator.AND, attribute);
                return this;
            }

            @Override
            public QueryBuilder<Value> or(String attribute) {
                apply(BooleanOperator.OR, attribute);
                return this;
            }

            private void apply(BooleanOperator operator, String attribute) {
                Predicate<String, Value> current = Predicates.and(predicates.toArray(new Predicate[0]));

                if (predicate == null) {
                    predicate = current;
                } else {
                    switch (this.operator) {
                        case AND -> predicate = Predicates.and(predicate, current);
                        case OR -> predicate = Predicates.or(predicate, current);
                    }
                }
                predicates.clear();
                this.operator = operator;
                setAttribute(attribute);
            }

            @Override
            public QueryBuilder<Value> between(Long minimum, Long maximum) {
                predicates.add(Predicates.between(attribute(), minimum, maximum));
                return this;
            }

            @Override
            public QueryBuilder<Value> like(String text) {
                predicates.add(Predicates.ilike(attribute(), "%" + text + "%"));
                return this;
            }

            @Override
            public QueryBuilder<Value> startsWith(String text) {
                predicates.add(Predicates.ilike(attribute(), text + "%"));
                return this;
            }

            @Override
            public QueryBuilder<Value> in(Comparable... list) {
                predicates.add(Predicates.in(attribute(), list));
                return this;
            }

            @Override
            public QueryBuilder<Value> equalTo(Comparable match) {
                predicates.add(Predicates.equal(attribute(), match));
                return this;
            }

            @Override
            public QueryBuilder<Value> matches(String regex) {
                predicates.add(Predicates.regex(attribute(), regex));
                return this;
            }

            @Override
            public Future<Collection<Value>> execute() {
                apply(operator, attribute());

                return context.<Collection<Value>>blocking(() -> imap.values(getPredicateWithPager()), false);
            }

            private PagingPredicate<String, Value> getPredicateWithPager() {
                PagingPredicate<String, Value> paging;

                if (isOrdered()) {
                    String orderBy = getOrderByAttribute();
                    int sortDirection = getSortDirection();

                    paging = Predicates.pagingPredicate(predicate,
                            (Serializable & Comparator<Map.Entry<String, Value>>) (first, second) ->
                                    first.getValue().compareToAttribute(second.getValue(), orderBy) * sortDirection,
                            getPageSize()
                    );
                } else {
                    paging = Predicates.pagingPredicate(predicate, getPageSize());
                }
                paging.setPage(getPage());
                return paging;
            }
        };
    }

    @Override
    public StorageContext<Value> context() {
        return context;
    }

    private enum BooleanOperator {AND, OR}
}
