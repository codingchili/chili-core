package com.codingchili.core.Storage;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.*;

import java.util.function.Consumer;

import com.codingchili.core.Context.StorageContext;


/**
 * @author Robin Duda
 *         <p>
 *         MongoDB backed asyncmap.
 */
public class AsyncMongoMap<Key, Value> implements AsyncStorage<Key, Value> {
    private static final JsonObject ALL_FIELDS = new JsonObject();
    private static final String ID = "_id";

    private StorageContext<Value> context;
    private MongoClient client;
    private String DB;

    public AsyncMongoMap(Future<AsyncStorage<Key, Value>> future, StorageContext<Value> context) {
        client = MongoClient.createShared(context.vertx(), new JsonObject());

        this.DB = context.DB();
        this.context = context;

        client.createIndexWithOptions(DB, new JsonObject().put(ID, 1), index(), handler -> {
            future.complete(this);
        });
    }

    private IndexOptions index() {
        return new IndexOptions()
                .unique(true);
    }

    @Override
    public void clear(Handler<AsyncResult<Void>> handler) {
        client.dropCollection(DB, handler);
    }

    @Override
    public void size(Handler<AsyncResult<Integer>> handler) {
        Future<Integer> future = Future.future();
        future.setHandler(handler);

        client.count(DB, new JsonObject(), result -> {
            if (result.succeeded()) {
                future.complete(result.result().intValue());
            } else {
                future.fail(result.cause());
            }
        });
    }

    @Override
    public void get(Key key, Handler<AsyncResult<Value>> handler) {
        client.findOne(DB, query(key), ALL_FIELDS, result -> convertJson(result, handler));
    }

    private void convertJson(AsyncResult<JsonObject> json, Handler<AsyncResult<Value>> handler) {
        handler.handle(context.convertJson(json));
    }

    private JsonObject query(Key key) {
        return new JsonObject().put(ID, key.toString());
    }

    @Override
    public void put(Key key, Value value, Handler<AsyncResult<Void>> handler) {
        client.replaceDocumentsWithOptions(DB, getKey(key), document(key, value),
                new UpdateOptions().setUpsert(true),
                update -> handler.handle(context.convertVoid(update)));
    }

    private JsonObject document(Key key, Value value) {
        return context.toJson(value).put(ID, key.toString());
    }

    @Override
    public void put(Key key, Value value, long ttl, Handler<AsyncResult<Void>> handler) {
        put(key, value, handler);

        context.timer(ttl, expired -> remove(key, removed -> {
        }));
    }

    @Override
    public void putIfAbsent(Key key, Value value, Handler<AsyncResult<Value>> handler) {
        client.insert(DB, document(key, value), put -> {
            if (put.succeeded()) {

                if (put.result() == null) {
                    handler.handle(Future.succeededFuture(null));
                } else {
                    handler.handle(Future.failedFuture(put.cause()));
                }

            } else {
                get(key, handler);
            }
        });
    }

    @Override
    public void putIfAbsent(Key key, Value value, long ttl, Handler<AsyncResult<Value>> handler) {
        putIfAbsent(key, value, handler);

        context.timer(ttl, event -> remove(key, result -> {
        }));
    }

    @Override
    public void remove(Key key, Handler<AsyncResult<Value>> handler) {
        Consumer<Value> remover = value -> client.removeDocument(DB, getKey(key), remove -> {
            if (remove.succeeded()) {
                handler.handle(Future.succeededFuture(value));
            } else {
                handler.handle(Future.failedFuture(remove.cause()));
            }
        });

        getByKey(key, remover, handler);
    }

    private void getByKey(Key key, Consumer<Value> consumer, Handler<AsyncResult<Value>> handler) {
        get(key, get -> {
            if (get.succeeded()) {
                consumer.accept(get.result());
            } else {
                handler.handle(Future.failedFuture(get.cause()));
            }
        });
    }

    private JsonObject getKey(Key key) {
        return new JsonObject().put(ID, key.toString());
    }

    @Override
    public void removeIfPresent(Key key, Value value, Handler<AsyncResult<Boolean>> handler) {
        client.removeDocument(DB, document(key, value), remove -> {
            if (remove.succeeded()) {
                handler.handle(Future.succeededFuture(remove.result().getRemovedCount() != 0));
            } else {
                handler.handle(Future.failedFuture(remove.cause()));
            }
        });
    }

    @Override
    public void replace(Key key, Value value, Handler<AsyncResult<Value>> handler) {
        Consumer<Value> consumer = previous -> {
            client.replaceDocuments(DB, getKey(key), document(key, value), replace -> {
                if (replace.succeeded()) {
                    handler.handle(Future.succeededFuture(previous));
                } else {
                    handler.handle(Future.failedFuture(replace.cause()));
                }
            });
        };

        getByKey(key, consumer, handler);
    }

    @Override
    public void replaceIfPresent(Key key, Value oldValue, Value newValue, Handler<AsyncResult<Boolean>> handler) {
        client.replaceDocuments(DB, document(key, oldValue), document(key, newValue), replace -> {
            if (replace.succeeded()) {
                handler.handle(Future.succeededFuture(replace.result().getDocModified() != 0));
            } else {
                handler.handle(Future.failedFuture(replace.cause()));
            }
        });
    }
}
