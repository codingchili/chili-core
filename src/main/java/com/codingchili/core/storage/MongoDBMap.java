package com.codingchili.core.storage;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.*;

import java.util.List;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.storage.exception.*;

import static com.codingchili.core.context.FutureHelper.failed;
import static com.codingchili.core.context.FutureHelper.succeeded;

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
    private String DB;

    public MongoDBMap(Future<AsyncStorage<Key, Value>> future, StorageContext<Value> context) {
        client = MongoClient.createShared(context.vertx(), new JsonObject());

        this.DB = context.DB();
        this.context = context;

        client.createIndexWithOptions(DB, new JsonObject().put(ID, 1), index(), index -> {
            future.complete(this);
        });
    }

    private IndexOptions index() {
        return new IndexOptions()
                .unique(true);
    }

    @Override
    public void get(Key key, Handler<AsyncResult<Value>> handler) {
        client.findOne(DB, query(key), ALL_FIELDS, query -> {
            if (query.succeeded()) {
                if (query.result() != null) {
                    handler.handle(succeeded(context.toValue(query.result())));
                } else {
                    handler.handle(failed(new MissingEntityException(key)));
                }
            } else {
                handler.handle(failed(query.cause()));
            }
        });
    }

    private JsonObject query(Key key) {
        return new JsonObject().put(ID, key.toString());
    }

    @Override
    public void put(Key key, Value value, Handler<AsyncResult<Void>> handler) {
        put(key, value, Long.MAX_VALUE, handler);
    }

    private JsonObject document(Key key, Value value) {
        return context.toJson(value).put(ID, key.toString());
    }

    @Override
    public void put(Key key, Value value, long ttl, Handler<AsyncResult<Void>> handler) {
        client.replaceDocumentsWithOptions(DB, getKey(key), document(key, value),
                new UpdateOptions().setUpsert(true),
                update -> {
                    if (update.succeeded()) {
                        context.timer(ttl, expired -> remove(key, (removed) -> {}));
                        handler.handle(succeeded());
                    } else {
                        handler.handle(failed(update.cause()));
                    }
                });
    }

    @Override
    public void putIfAbsent(Key key, Value value, Handler<AsyncResult<Void>> handler) {
        putIfAbsent(key, value, Long.MAX_VALUE, handler);
    }

    @Override
    public void putIfAbsent(Key key, Value value, long ttl, Handler<AsyncResult<Void>> handler) {
        client.insert(DB, document(key, value), put -> {
            if (put.succeeded()) {
                context.timer(ttl, expired -> remove(key, (removed) -> {}));
                handler.handle(succeeded());
            } else {
                handler.handle(failed(new ValueAlreadyPresentException(key)));
            }
        });
    }

    @Override
    public void remove(Key key, Handler<AsyncResult<Void>> handler) {
        client.removeDocument(DB, getKey(key), remove -> {
            if (remove.succeeded()) {
                if (remove.result().getRemovedCount() > 0) {
                    handler.handle(succeeded());
                } else {
                    handler.handle(failed(new NothingToRemoveException(key)));
                }
            } else {
                handler.handle(failed(remove.cause()));
            }
        });
    }

    private JsonObject getKey(Key key) {
        return new JsonObject().put(ID, key.toString());
    }

    @Override
    public void replace(Key key, Value value, Handler<AsyncResult<Void>> handler) {
        client.replaceDocuments(DB, getKey(key), document(key, value), replace -> {
            if (replace.succeeded()) {
                if (replace.result().getDocModified() > 0) {
                    handler.handle(succeeded());
                } else {
                    handler.handle(failed(new NothingToReplaceException(key)));
                }
            } else {
                handler.handle(failed(replace.cause()));
            }
        });
    }

    @Override
    public void clear(Handler<AsyncResult<Void>> handler) {
        client.dropCollection(DB, drop -> {
            if (drop.succeeded()) {
                handler.handle(succeeded());
            } else {
                handler.handle(failed(drop.cause()));
            }
        });
    }

    @Override
    public void size(Handler<AsyncResult<Integer>> handler) {
        client.count(DB, new JsonObject(), result -> {
            if (result.succeeded()) {
                handler.handle(succeeded(result.result().intValue()));
            } else {
                handler.handle(failed(result.cause()));
            }
        });
    }

    @Override
    public void queryExact(JsonObject attributes, Handler<AsyncResult<List<Value>>> handler) {

    }

    @Override
    public void querySimilar(JsonObject attributes, Handler<AsyncResult<List<Value>>> handler) {

    }

    @Override
    public void queryRange(int from, int to, Handler<AsyncResult<List<Value>>> handler, String... attributes) {

    }
}
