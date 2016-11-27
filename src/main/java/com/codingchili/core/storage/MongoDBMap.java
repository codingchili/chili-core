package com.codingchili.core.storage;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.*;

import java.util.*;
import java.util.stream.Collectors;

import com.codingchili.core.context.FutureHelper;
import com.codingchili.core.context.StorageContext;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.storage.exception.*;

import static com.codingchili.core.context.FutureHelper.error;
import static com.codingchili.core.context.FutureHelper.result;

/**
 * @author Robin Duda
 *         <p>
 *         mongodb backed asyncmap.
 */
public class MongoDBMap<Key, Value> implements AsyncStorage<Key, Value> {
    private static final JsonObject ALL_FIELDS = new JsonObject();
    private static final String ID = "_id";
    private StorageContext<Value> context;
    private MongoClient client;
    private String collection;

    public MongoDBMap(Future<AsyncStorage<Key, Value>> future, StorageContext<Value> context) {
        client = MongoClient.createShared(context.vertx(), Serializer.json(context.storage()));

        this.collection = context.collection();
        this.context = context;

        client.createIndexWithOptions(collection, new JsonObject().put(ID, 1), index(), index -> {
            future.complete(this);
        });
    }

    private IndexOptions index() {
        return new IndexOptions()
                .unique(true);
    }

    @Override
    public void get(Key key, Handler<AsyncResult<Value>> handler) {
        client.findOne(collection, query(key), ALL_FIELDS, query -> {
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

    private JsonObject query(Key key) {
        return new JsonObject().put(ID, key.toString());
    }

    @Override
    public void put(Key key, Value value, Handler<AsyncResult<Void>> handler) {
        client.replaceDocumentsWithOptions(collection, getKey(key), document(key, value),
                new UpdateOptions().setUpsert(true),
                update -> {
                    if (update.succeeded()) {
                        handler.handle(result());
                    } else {
                        handler.handle(error(update.cause()));
                    }
                });
    }

    private JsonObject document(Key key, Value value) {
        return context.toJson(value).put(ID, key.toString());
    }

    @Override
    public void put(Key key, Value value, long ttl, Handler<AsyncResult<Void>> handler) {
        put(key, value, result -> {
            if (result.succeeded()) {
                context.timer(ttl, expired -> remove(key, (removed) -> {}));
                handler.handle(result(result.result()));
            } else {
                handler.handle(error(result.cause()));
            }
        });
    }

    @Override
    public void putIfAbsent(Key key, Value value, Handler<AsyncResult<Void>> handler) {
        client.insert(collection, document(key, value), put -> {
            if (put.succeeded()) {
                handler.handle(FutureHelper.result());
            } else {
                handler.handle(error(new ValueAlreadyPresentException(key)));
            }
        });
    }

    @Override
    public void putIfAbsent(Key key, Value value, long ttl, Handler<AsyncResult<Void>> handler) {
        put(key, value, result -> {
            if (result.succeeded()) {
                context.timer(ttl, expired -> remove(key, (removed) -> {}));
                handler.handle(result());
            } else {
                handler.handle(error(result.cause()));
            }
        });
    }

    @Override
    public void remove(Key key, Handler<AsyncResult<Void>> handler) {
        client.removeDocument(collection, getKey(key), remove -> {
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

    private JsonObject getKey(Key key) {
        return new JsonObject().put(ID, key.toString());
    }

    @Override
    public void replace(Key key, Value value, Handler<AsyncResult<Void>> handler) {
        client.replaceDocuments(collection, getKey(key), document(key, value), replace -> {
            if (replace.succeeded()) {
                if (replace.result().getDocModified() > 0) {
                    handler.handle(FutureHelper.result());
                } else {
                    handler.handle(error(new NothingToReplaceException(key)));
                }
            } else {
                handler.handle(error(replace.cause()));
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

    @Override
    public void queryExact(String attribute, Comparable compare, Handler<AsyncResult<Collection<Value>>> handler) {
        client.find(collection, query(attribute, compare), query -> {
            if (query.succeeded()) {
                handler.handle(result(toList(query.result())));
            } else {
                handler.handle(error(query.cause()));
            }
        });
    }

    private Collection<Value> toList(List<JsonObject> results) {
        return results.stream().map(json -> context.toValue(json)).collect(Collectors.toList());
    }

    private JsonObject query(String attribute, Comparable compare) {
        return new JsonObject().put(attribute, compare);
    }

    @Override
    public void querySimilar(String attribute, Comparable comparable, Handler<AsyncResult<Collection<Value>>> handler) {
        if (context.validate(comparable)) {
            client.find(collection, queryLike(attribute, comparable), query -> {
                if (query.succeeded()) {
                    handler.handle(result(toList(query.result())));
                } else {
                    handler.handle(error(query.cause()));
                }
            });
        } else {
            handler.handle(result(new ArrayList<>()));
        }
    }

    private JsonObject queryLike(String attribute, Comparable compare) {
        return new JsonObject().put(attribute, "/^" + compare + "/");
    }

    @Override
    public void queryRange(String attribute, int from, int to, Handler<AsyncResult<Collection<Value>>> handler) {
        client.find(collection, rangeQuery(attribute, from, to), query -> {
            if (query.succeeded()) {
                handler.handle(result(toList(query.result())));
            } else {
                handler.handle(error(query.cause()));
            }
        });
    }

    private JsonObject rangeQuery(String attribute, int from, int to) {
        return new JsonObject().put(attribute,
                new JsonObject()
                        .put("$gte", from)
                        .put("$lte", to)
        );
    }
}
