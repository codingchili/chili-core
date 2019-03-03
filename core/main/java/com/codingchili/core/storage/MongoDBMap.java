package com.codingchili.core.storage;

import io.vertx.core.*;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.*;

import java.util.*;
import java.util.stream.Collectors;

import com.codingchili.core.context.FutureHelper;
import com.codingchili.core.context.StorageContext;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.Validator;
import com.codingchili.core.storage.exception.*;

import static com.codingchili.core.context.FutureHelper.*;

/**
 * mongodb backed asyncmap.
 */
public class MongoDBMap<Value extends Storable> implements AsyncStorage<Value> {
    private static final JsonObject ALL_FIELDS = new JsonObject();
    private static final String ID = "_id";
    private static final String AND = "$and";
    private static final String OR = "$or";
    private static final String GTE = "$gte";
    private static final String LTE = "$lte";
    private static final String REGEX = "$regex";
    private static final String IN = "$in";
    private static final String OPTIONS = "$options";
    private Set<String> indexed = new ConcurrentHashSet<>();
    private StorageContext<Value> context;
    private MongoClient client;
    private String collection;

    public MongoDBMap(Future<AsyncStorage<Value>> future, StorageContext<Value> context) {
        client = MongoClient.createShared(context.vertx(), Serializer.json(context.storage()));

        this.collection = context.collection();
        this.context = context;

        addIndex(ID, done -> {
            future.complete(this);
        });
    }

    private IndexOptions index() {
        return new IndexOptions()
                .unique(true);
    }

    @Override
    public void get(String key, Handler<AsyncResult<Value>> handler) {
        client.findOne(collection, id(key), ALL_FIELDS, query -> {
            if (query.succeeded()) {
                if (query.result() != null) {
                    handler.handle(result(context.toValue(query.result())));
                } else {
                    handler.handle(error(new ValueMissingException(key)));
                }
            } else {
                handler.handle(error(query.cause()));
            }
        });
    }

    @Override
    public void put(Value value, Handler<AsyncResult<Void>> handler) {
        client.replaceDocumentsWithOptions(collection, id(value.getId()), document(value),
                new UpdateOptions().setUpsert(true),
                update -> {
                    if (update.succeeded()) {
                        handler.handle(result());
                    } else {
                        handler.handle(error(update.cause()));
                    }
                });
    }

    private JsonObject document(Value value) {
        return context.toJson(value).put(ID, value.getId());
    }

    @Override
    public void putIfAbsent(Value value, Handler<AsyncResult<Void>> handler) {
        client.insert(collection, document(value), put -> {
            if (put.succeeded()) {
                handler.handle(FutureHelper.result());
            } else {
                handler.handle(error(new ValueAlreadyPresentException(value.getId())));
            }
        });
    }

    @Override
    public void remove(String key, Handler<AsyncResult<Void>> handler) {
        client.removeDocument(collection, id(key), remove -> {
            if (remove.succeeded()) {
                if (remove.result().getRemovedCount() > 0) {
                    handler.handle(FutureHelper.result());
                } else {
                    handler.handle(error(new NothingToRemoveException(key)));
                }
            } else {
                handler.handle(error(remove.cause()));
            }
        });
    }

    private JsonObject id(String key) {
        return new JsonObject().put(ID, key);
    }

    private JsonObject id(Value value) {
        return id(value.getId());
    }

    @Override
    public void update(Value value, Handler<AsyncResult<Void>> handler) {
        client.replaceDocuments(collection, id(value), document(value), replace -> {
            if (replace.succeeded()) {
                if (replace.result().getDocModified() > 0) {
                    handler.handle(FutureHelper.result());
                } else {
                    handler.handle(error(new NothingToUpdateException(value.getId())));
                }
            } else {
                handler.handle(error(replace.cause()));
            }
        });
    }

    @Override
    public void values(Handler<AsyncResult<Collection<Value>>> handler) {
        client.find(collection, new JsonObject(), found -> {
            if (found.succeeded()) {
                handler.handle(result(toList(found.result())));
            } else {
                handler.handle(Future.failedFuture(found.cause()));
            }
        });
    }

    @Override
    public void clear(Handler<AsyncResult<Void>> handler) {
        client.dropCollection(collection, drop -> {
            if (drop.succeeded()) {
                handler.handle(FutureHelper.result());
            } else {
                handler.handle(error(drop.cause()));
            }
        });
    }

    @Override
    public void size(Handler<AsyncResult<Integer>> handler) {
        client.count(collection, new JsonObject(), result -> {
            if (result.succeeded()) {
                handler.handle(result(result.result().intValue()));
            } else {
                handler.handle(error(result.cause()));
            }
        });
    }

    private void addIndex(String field) {
        addIndex(field, (result) -> {
        });
    }

    private void addIndex(String field, Handler<AsyncResult<Void>> handler) {
        if (!indexed.contains(field)) {
            indexed.add(field);
            client.createIndex(context.collection(), new JsonObject().put(field, ""), handler);
        }
    }

    @Override
    public QueryBuilder<Value> query() {
        return new AbstractQueryBuilder<Value>(this) {
            JsonArray statements = new JsonArray();
            JsonArray builder = new JsonArray();

            @Override
            public QueryBuilder<Value> on(String attribute) {
                addIndex(attribute);
                setAttribute(attribute);
                return this;
            }

            @Override
            public QueryBuilder<Value> and(String attribute) {
                addIndex(attribute);
                setAttribute(attribute);
                return this;
            }

            @Override
            public QueryBuilder<Value> or(String attribute) {
                addIndex(attribute);
                setAttribute(attribute);
                apply();
                return this;
            }

            /** Applies the current state of the builder to the final query. */
            private void apply() {
                statements.add(new JsonObject().put(AND, builder));
                builder = new JsonArray();
            }

            @Override
            public QueryBuilder<Value> between(Long minimum, Long maximum) {
                builder.add(new JsonObject()
                        .put(attribute(), new JsonObject()
                                .put(GTE, minimum)
                                .put(LTE, maximum)));
                return this;
            }

            @Override
            public QueryBuilder<Value> like(String text) {
                text = Validator.toPlainText(text);
                builder.add(new JsonObject()
                        .put(attribute(), new JsonObject()
                                .put(REGEX, "^.*" + text + ".*$")
                                .put(OPTIONS, "i")));
                return this;
            }

            @Override
            public QueryBuilder<Value> startsWith(String text) {
                text = Validator.toPlainText(text);
                builder.add(new JsonObject()
                        .put(attribute(), new JsonObject()
                                .put(REGEX, "^" + text + ".*")));
                return this;
            }

            @Override
            public QueryBuilder<Value> in(Comparable... comparables) {
                List<Comparable> list = new ArrayList<>();
                list.addAll(Arrays.asList(comparables));

                builder.add(new JsonObject()
                        .put(attribute(), new JsonObject()
                                .put(IN, list)));
                return this;
            }

            @Override
            public QueryBuilder<Value> equalTo(Comparable match) {
                builder.add(new JsonObject().put(attribute(), match));
                return this;
            }

            @Override
            public QueryBuilder<Value> matches(String regex) {
                builder.add(new JsonObject()
                        .put(attribute(), new JsonObject()
                                .put(REGEX, regex)));
                return this;
            }

            @Override
            public void execute(Handler<AsyncResult<Collection<Value>>> handler) {
                apply();

                client.findWithOptions(collection, new JsonObject().put(OR, statements), getOptions(), find -> {
                    if (find.succeeded()) {
                        handler.handle(result(toList(find.result())));
                    } else {
                        handler.handle(error(find.cause()));
                    }
                });
            }

            private FindOptions getOptions() {
                return new FindOptions()
                        .setLimit(pageSize)
                        .setSkip(pageSize * page)
                        .setSort(getSortOptions());
            }

            private JsonObject getSortOptions() {
                if (isOrdered) {
                    return new JsonObject().put(getOrderByAttribute(), getSortDirection());
                } else {
                    return new JsonObject();
                }
            }
        };
    }

    @Override
    public StorageContext<Value> context() {
        return context;
    }

    private List<Value> toList(Collection<JsonObject> results) {
        return results.stream().map(json -> context.toValue(json)).collect(Collectors.toList());
    }
}
