package com.codingchili.core.storage;

import com.hazelcast.core.*;
import com.hazelcast.query.*;
import io.vertx.core.*;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.shareddata.AsyncMap;

import java.io.Serializable;
import java.util.*;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.FutureHelper;
import com.codingchili.core.context.StorageContext;
import com.codingchili.core.storage.exception.*;

import static com.codingchili.core.configuration.CoreStrings.STORAGE_ARRAY;
import static com.codingchili.core.context.FutureHelper.*;

/**
 * Initializes a new hazel async map.
 */
public class HazelMap<Value extends Storable> implements AsyncStorage<Value> {
    private static final String HAZEL_ARRAY = "[any]";
    private Set<String> indexed = new ConcurrentHashSet<>();
    private StorageContext<Value> context;
    private AsyncMap<String, Value> map;
    private IMap<String, Value> imap;

    /**
     * Initializes a new hazel async map.
     *
     * @param context the context requesting the map to be created.
     * @param future  called when the map is created.
     */
    public HazelMap(Future<AsyncStorage> future, StorageContext<Value> context) {
        this.context = context;

        context.vertx().sharedData().<String, Value>getClusterWideMap(context.collection(), cluster -> {
            if (cluster.succeeded()) {
                this.map = cluster.result();

                Optional<HazelcastInstance> hazel = Hazelcast.getAllHazelcastInstances().stream().findFirst();

                if (hazel.isPresent()) {
                    HazelcastInstance instance = hazel.get();
                    imap = instance.getMap(context.collection());
                    addIndex(Storable.idField);
                    future.complete(this);
                } else {
                    future.fail(CoreStrings.ERROR_NOT_CLUSTERED);
                }
            } else {
                future.fail(cluster.cause());
            }
        });
    }

    @Override
    public void get(String key, Handler<AsyncResult<Value>> handler) {
        map.get(key, get -> {
            if (get.succeeded()) {
                if (get.result() != null) {
                    handler.handle(result(get.result()));
                } else {
                    handler.handle(error(new ValueMissingException(key)));
                }
            } else {
                handler.handle(error(get.cause()));
            }
        });
    }

    @Override
    public void put(Value value, Handler<AsyncResult<Void>> handler) {
        map.put(value.getId(), value, handler);
    }

    @Override
    public void putIfAbsent(Value value, Handler<AsyncResult<Void>> handler) {
        map.putIfAbsent(value.getId(), value, put -> {
            if (put.succeeded()) {
                if (put.result() == null) {
                    handler.handle(FutureHelper.result());
                } else {
                    handler.handle(error(new ValueAlreadyPresentException(value.getId())));
                }
            } else {
                handler.handle(error(put.cause()));
            }
        });
    }

    @Override
    public void remove(String key, Handler<AsyncResult<Void>> handler) {
        map.remove(key, remove -> {
            if (remove.succeeded()) {
                if (remove.result() == null) {
                    handler.handle(error(new NothingToRemoveException(key)));
                } else {
                    handler.handle(FutureHelper.result());
                }
            } else {
                handler.handle(error(remove.cause()));
            }
        });
    }

    @Override
    public void update(Value value, Handler<AsyncResult<Void>> handler) {
        map.replace(value.getId(), value, replace -> {
            if (replace.succeeded()) {
                if (replace.result() == null) {
                    handler.handle(error(new NothingToUpdateException(value.getId())));
                } else {
                    handler.handle(FutureHelper.result());
                }
            } else {
                handler.handle(error(replace.cause()));
            }
        });
    }

    @Override
    public void values(Handler<AsyncResult<Collection<Value>>> handler) {
        context.<Collection<Value>>blocking(blocked -> {
            blocked.complete(imap.values());
        }, completed -> {
            handler.handle(result(completed.result()));
        });

    }

    @Override
    public void clear(Handler<AsyncResult<Void>> handler) {
        map.clear(clear -> {
            if (clear.succeeded()) {
                handler.handle(FutureHelper.result());
            } else {
                handler.handle(error(clear.cause()));
            }
        });
    }

    @Override
    public void size(Handler<AsyncResult<Integer>> handler) {
        map.size(size -> {
            if (size.succeeded()) {
                handler.handle(result(size.result()));
            } else {
                handler.handle(error(size.cause()));
            }
        });
    }

    private void addIndex(String field) {
        if (!indexed.contains(field)) {
            indexed.add(field);
            imap.addIndex(field.replace(STORAGE_ARRAY, HAZEL_ARRAY), false);
        }
    }

    @Override
    public QueryBuilder<Value> query() {

        return new AbstractQueryBuilder<Value>(this, HAZEL_ARRAY) {
            private List<Predicate<String, Value>> predicates = new ArrayList<>();
            private Predicate<String, Value> predicate;
            private BooleanOperator operator = BooleanOperator.AND;

            @Override
            public QueryBuilder<Value> on(String attribute) {
                addIndex(attribute);
                setAttribute(attribute);
                return this;
            }

            @Override
            public QueryBuilder<Value> and(String attribute) {
                addIndex(attribute);
                apply(BooleanOperator.AND, attribute);
                return this;
            }

            @Override
            public QueryBuilder<Value> or(String attribute) {
                addIndex(attribute);
                apply(BooleanOperator.OR, attribute);
                return this;
            }

            @SuppressWarnings("unchecked")
            private void apply(BooleanOperator operator, String attribute) {
                Predicate<String, Value> current = Predicates.and(predicates.toArray(new Predicate[predicates.size()]));

                if (predicate == null) {
                    predicate = current;
                } else {
                    switch (this.operator) {
                        case AND:
                            predicate = Predicates.and(predicate, current);
                            break;
                        case OR:
                            predicate = Predicates.or(predicate, current);
                            break;
                    }
                }
                predicates.clear();
                this.operator = operator;
                setAttribute(attribute);
            }

            @Override
            @SuppressWarnings("unchecked")
            public QueryBuilder<Value> between(Long minimum, Long maximum) {
                predicates.add(Predicates.between(attribute(), minimum, maximum));
                return this;
            }

            @Override
            @SuppressWarnings("unchecked")
            public QueryBuilder<Value> like(String text) {
                predicates.add(Predicates.ilike(attribute(), "%" + text + "%"));
                return this;
            }

            @Override
            @SuppressWarnings("unchecked")
            public QueryBuilder<Value> startsWith(String text) {
                predicates.add(Predicates.ilike(attribute(), text + "%"));
                return this;
            }

            @Override
            @SuppressWarnings("unchecked")
            public QueryBuilder<Value> in(Comparable... list) {
                predicates.add(Predicates.in(attribute(), list));
                return this;
            }

            @Override
            @SuppressWarnings("unchecked")
            public QueryBuilder<Value> equalTo(Comparable match) {
                predicates.add(Predicates.equal(attribute(), match));
                return this;
            }

            @Override
            @SuppressWarnings("unchecked")
            public QueryBuilder<Value> matches(String regex) {
                predicates.add(Predicates.regex(attribute(), regex));
                return this;
            }

            @Override
            public void execute(Handler<AsyncResult<Collection<Value>>> handler) {
                apply(operator, attribute());

                context.<Collection<Value>>blocking(task -> {
                    task.complete(imap.values(getPredicateWithPager()));
                }, false, result -> {
                    if (result.succeeded()) {
                        handler.handle(result(result.result()));
                    } else {
                        handler.handle(error(result.cause()));
                    }
                });
            }

            private Predicate getPredicateWithPager() {
                PagingPredicate<String, Value> paging;

                if (isOrdered) {
                    String orderBy = getOrderByAttribute();
                    int sortDirection = getSortDirection();
                    paging = new PagingPredicate<>(predicate, (Serializable & Comparator<Map.Entry<String, Value>>) (first, second) -> {
                        return first.getValue().compareToAttribute(second.getValue(), orderBy) * sortDirection;
                    }, pageSize);
                } else {
                    paging = new PagingPredicate<>(predicate, pageSize);
                }
                paging.setPage(page);
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
