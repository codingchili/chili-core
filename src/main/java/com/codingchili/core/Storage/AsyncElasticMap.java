package com.codingchili.core.Storage;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.Context.StorageContext;
import com.codingchili.core.Exception.StorageFailureException;
import com.codingchili.core.Testing.ContextMock;

import static com.codingchili.core.Configuration.Strings.DIR_SEPARATOR;


/**
 * @author Robin Duda
 *         <p>
 *         Mocks an async map used by Hazelcast to enable testing of storage logic.
 */
public class AsyncElasticMap<Key, Value> implements AsyncStorage<Key, Value> {
    private static final String STATUS_CREATED = "created";
    private static final String STATUS_UPDATED = "updated";
    private static final String ID_COUNT = "count";
    private static final String ID_RESULT = "result";
    private static final String ID_FOUND = "found";
    private static final String PARAM_SOURCE = "_source";
    private static final String PARAM_COUNT = "_count";
    private static final String OP_CREATE = "?op_type=create";
    private StorageContext<Value> context;

    public static void main(String[] args) {
        Future<AsyncStorage<String, JsonObject>> future = Future.future();

        future.setHandler(map -> {
            AsyncStorage<String, JsonObject> storage = map.result();
            storage.put("key", new JsonObject().put("one", "two"), get -> {

            });
        });

        StorageLoader.prepare()
                .withContext(new StorageContext<>(new ContextMock(Vertx.vertx())))
                .withDB("test-db")
                .withPlugin(AsyncElasticMap.class)
                .withClass(JsonObject.class)
                .build(future);
    }

    public AsyncElasticMap(Future<AsyncStorage<Key, Value>> future, StorageContext<Value> context) {
        this.context = context;
        future.complete(this);
    }

    private AsyncElasticMap() {
    }

    @Override
    public void get(Key key, Handler<AsyncResult<Value>> handler) {
        getRequest(key, response -> {
            JsonObject reply = response.toJsonObject();

            if (reply.containsKey(ID_FOUND) && reply.getBoolean(ID_FOUND)) {
                handler.handle(Future.succeededFuture(context.toValue(reply.getJsonObject(PARAM_SOURCE))));
            } else {
                handler.handle(Future.succeededFuture());
            }
        });
    }

    private String entryPath(Key key) {
        return context.DB() + DIR_SEPARATOR +
                context.collection() + DIR_SEPARATOR +
                key.toString();
    }

    private void getRequest(Key key, Handler<Buffer> bodyHandler) {
        context.vertx().createHttpClient()
                .get(9200, "localhost", entryPath(key))
                .handler(response -> response.bodyHandler(bodyHandler)).end();
    }

    @Override
    public void put(Key key, Value value, Handler<AsyncResult<Void>> handler) {
        putRequest(key, value, response -> {
            JsonObject reply = response.toJsonObject();

            if (createdOrUpdated(reply)) {
                handler.handle(Future.succeededFuture());
            } else {
                handler.handle(Future.failedFuture(new StorageFailureException()));
            }
        });
    }

    private boolean createdOrUpdated(JsonObject reply) {
        if (reply.containsKey(ID_RESULT)) {
            String result = reply.getString(ID_RESULT);
            return (STATUS_CREATED.equals(result) || STATUS_UPDATED.equals(result));
        } else {
            return false;
        }
    }

    private void putRequest(Key key, Value value, Handler<Buffer> bodyHandler) {
        context.vertx().createHttpClient()
                .put(9200, "localhost", entryPath(key))
                .handler(response -> response.bodyHandler(bodyHandler))
                .end(Buffer.buffer(context.toJson(value).encode()));
    }

    private String putIfAbsentPath(Key key) {
        return context.DB() + DIR_SEPARATOR +
                context.collection() + DIR_SEPARATOR +
                key.toString() + OP_CREATE;
    }

    @Override
    public void put(Key key, Value value, long ttl, Handler<AsyncResult<Void>> handler) {
        put(key, value, handler);
        context.timer(ttl, event -> remove(key, result -> {
        }));
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
    public void replaceIfPresent(Key key, Value oldValue, Value newValue, Handler<AsyncResult<Boolean>> handler) {

    }

    @Override
    public void clear(Handler<AsyncResult<Void>> handler) {
        context.vertx().createHttpClient().delete(9200, "localhost",
                DIR_SEPARATOR + context.DB())
                .handler(result -> handler.handle(Future.succeededFuture())).end();
    }

    @Override
    public void size(Handler<AsyncResult<Integer>> handler) {
        context.vertx().createHttpClient().get(9200, "localhost",
                DIR_SEPARATOR + context.DB() +
                DIR_SEPARATOR + context.collection() +
                DIR_SEPARATOR + PARAM_COUNT)
                .handler(result -> {
                    result.bodyHandler(body -> {
                        JsonObject reply = body.toJsonObject();

                        if (reply.containsKey(ID_COUNT)) {
                            handler.handle(Future.succeededFuture(reply.getInteger(ID_COUNT)));
                        } else {
                            handler.handle(Future.succeededFuture(0));
                        }
                    });
                }).end();
    }
}
