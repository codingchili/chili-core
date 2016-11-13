package com.codingchili.core.Storage;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.IndexOptions;
import io.vertx.ext.mongo.MongoClient;

import com.codingchili.core.Context.StorageContext;

import static com.codingchili.core.Configuration.Strings.ID;

/**
 * @author Robin Duda
 *         <p>
 *         MongoDB backed asyncmap.
 */
public class AsyncMongoMap<Key, Value> implements AsyncStorage<Key, Value> {
    private static final JsonObject ALL_FIELDS = new JsonObject().put("_id", 0);
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
        client.insert(DB, document(key, value), result -> convertVoid(result, handler));
    }

    private void convertVoid(AsyncResult<String> string, Handler<AsyncResult<Void>> hVoid) {
        hVoid.handle(context.convertVoid(string));
    }

    private JsonObject document(Key key, Value value) {
        return context.serialize(value).put(ID, key);
    }

    @Override
    public void put(Key key, Value value, long ttl, Handler<AsyncResult<Void>> handler) {
        put(key, value, handler);
        context.timer(ttl, expired -> remove(key, removed -> {}));
    }

    @Override
    public void putIfAbsent(Key key, Value value, Handler<AsyncResult<Value>> handler) {

    }

    @Override
    public void putIfAbsent(Key key, Value value, long ttl, Handler<AsyncResult<Value>> handler) {

    }

    @Override
    public void remove(Key key, Handler<AsyncResult<Value>> handler) {

    }

    @Override
    public void removeIfPresent(Key key, Value value, Handler<AsyncResult<Boolean>> handler) {

    }

    @Override
    public void replace(Key key, Value value, Handler<AsyncResult<Value>> handler) {

    }

    @Override
    public void replaceIfPresent(Key key, Value oldValuealue, Value newValuealue, Handler<AsyncResult<Boolean>> handler) {

    }
}
