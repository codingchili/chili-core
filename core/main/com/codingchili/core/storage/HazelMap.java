package com.codingchili.core.storage;

import com.hazelcast.core.*;
import com.hazelcast.query.*;
import io.vertx.core.*;
import io.vertx.core.shareddata.AsyncMap;

import java.util.*;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.FutureHelper;
import com.codingchili.core.context.StorageContext;
import com.codingchili.core.security.Validator;
import com.codingchili.core.storage.exception.*;

import static com.codingchili.core.context.FutureHelper.*;
import static com.codingchili.core.files.Configurations.system;

/**
 * @author Robin Duda
 *         <p>
 *         Initializes a new hazel async map.
 */
public class HazelMap<Key, Value> implements AsyncStorage<Key, Value> {
    private static final String HAZELMAP_WORKERS = "HAZELMAP.workers";
    private WorkerExecutor executor;
    private StorageContext<Value> context;
    private AsyncMap<Key, Value> map;
    private IMap<Key, Value> imap;

    /**
     * Initializes a new hazel async map.
     *
     * @param context the context requesting the map to be created.
     * @param future  called when the map is created.
     */
    public HazelMap(Future<AsyncStorage> future, StorageContext<Value> context) {
        this.executor = context.vertx().createSharedWorkerExecutor(HAZELMAP_WORKERS, system().getWorkerPoolSize());
        this.context = context;

        context.vertx().sharedData().<Key, Value>getClusterWideMap(context.DB(), cluster -> {
            if (cluster.succeeded()) {
                this.map = cluster.result();

                Optional<HazelcastInstance> hazel = Hazelcast.getAllHazelcastInstances().stream().findFirst();

                if (hazel.isPresent()) {
                    HazelcastInstance instance = hazel.get();
                    imap = instance.getMap(context.DB());
                    future.complete(this);
                } else {
                    future.fail(CoreStrings.ERROR_CLUSTERING_REQUIRED);
                }
            } else {
                future.fail(cluster.cause());
            }
        });
    }

    @Override
    public void get(Key key, Handler<AsyncResult<Value>> handler) {
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
    public void put(Key key, Value value, Handler<AsyncResult<Void>> handler) {
        map.put(key, value, handler);
    }

    @Override
    public void put(Key key, Value value, long ttl, Handler<AsyncResult<Void>> handler) {
        map.put(key, value, ttl, handler);
    }

    @Override
    public void putIfAbsent(Key key, Value value, Handler<AsyncResult<Void>> handler) {
        map.putIfAbsent(key, value, put -> {
            if (put.succeeded()) {
                if (put.result() == null) {
                    handler.handle(FutureHelper.result());
                } else {
                    handler.handle(error(new ValueAlreadyPresentException(key)));
                }
            } else {
                handler.handle(error(put.cause()));
            }
        });
    }

    @Override
    public void putIfAbsent(Key key, Value value, long ttl, Handler<AsyncResult<Void>> handler) {
        map.putIfAbsent(key, value, ttl, result -> {
            if (result.succeeded()) {
                handler.handle(result());
            } else {
                handler.handle(error(result.cause()));
            }
        });
    }

    @Override
    public void remove(Key key, Handler<AsyncResult<Void>> handler) {
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
    public void replace(Key key, Value value, Handler<AsyncResult<Void>> handler) {
        map.replace(key, value, replace -> {
            if (replace.succeeded()) {
                if (replace.result() == null) {
                    handler.handle(error(new NothingToReplaceException(key)));
                } else {
                    handler.handle(FutureHelper.result());
                }
            } else {
                handler.handle(error(replace.cause()));
            }
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

        query("numbers")
                .between(5, 45)
                .in(6, 7, 8)
                .like("51")
                .and("name")
                .like("robin")
        .execute(result -> {
            result.result();
        });
    }

    @Override
    public QueryBuilder query(String attribute) {
        return new QueryBuilderBase<Value>() {
            private PredicateBuilder builder = new PredicateBuilder();
            private BooleanOperator operator = builder::and;

            @Override
            public QueryBuilder and(String attribute) {
                operator = builder::and;
                this.attribute = attribute;
                return this;
            }

            @Override
            public QueryBuilder or(String attribute) {
                operator = builder::or;
                this.attribute = attribute;
                return this;
            }

            @Override
            public QueryBuilder between(int minimum, int maximum) {
                operator.apply(Predicates.between(attribute, minimum, maximum));
                return this;
            }

            @Override
            public QueryBuilder like(String text) {
                operator.apply(Predicates.like(attribute, text));
                return this;
            }

            @Override
            public QueryBuilder startsWith(String text) {
                if (new Validator().plainText(text)) {
                    operator.apply(Predicates.regex(attribute, "^" + text));
                }
                return this;
            }

            @Override
            public QueryBuilder in(Comparable... list) {
                operator.apply(Predicates.in(attribute, list));
                return this;
            }

            @Override
            public void execute(Handler<AsyncResult<Collection<Value>>> handler) {
                executor.<Collection<Value>>executeBlocking(blocking -> {
                    PagingPredicate paging = new PagingPredicate(getPageSize());
                    paging.setPage(getPage());

                    blocking.complete(imap.values(builder.and(paging)));
                }, false, result -> {
                    if (result.succeeded()) {
                        handler.handle(result(result.result()));
                    } else {
                        handler.handle(error(result.cause()));
                    }
                });
            }
        };
    }

    @FunctionalInterface
    private interface BooleanOperator {
        void apply(Predicate predicate);
    }
}
