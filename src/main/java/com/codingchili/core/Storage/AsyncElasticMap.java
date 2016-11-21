package com.codingchili.core.Storage;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.Context.StorageContext;
import com.codingchili.core.Exception.StorageFailureException;

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
    private static final String PARAM_COUNT = "_search?size=0";
    private static final String OP_CREATE = "?op_type=create";
    private StorageContext<Value> context;

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
        return  DIR_SEPARATOR + context.DB() + DIR_SEPARATOR +
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
        context.vertx().createHttpClient().put(9200, "localhost", putIfAbsentPath(key)).handler(response -> {
            response.bodyHandler(body -> {
                if (isCreated(body.toJsonObject())) {
                    handler.handle(Future.succeededFuture());
                } else {
                    get(key, handler);
                }
            });
        }).end(context.toJson(value).encodePrettily());
    }

    private Value valueFromBody(Buffer body) {
        return context.toValue(body.toJsonObject().getJsonObject(PARAM_SOURCE));
    }

    private boolean isCreated(JsonObject reply) {
        return (STATUS_CREATED.equals(reply.getString(ID_RESULT)));
    }

    @Override
    public void putIfAbsent(Key key, Value value, long ttl, Handler<AsyncResult<Value>> handler) {
        putIfAbsent(key, value, handler);

        context.timer(ttl, event -> {
           remove(key, remove -> {});
        });
    }

    @Override
    public void remove(Key key, Handler<AsyncResult<Value>> handler) {
        get(key, found -> {
            if (found.result() != null) {
                context.vertx().createHttpClient().delete(9200, "localhost", entryPath(key), response -> {
                    response.bodyHandler(body -> {

                        if (isRemoved(body.toJsonObject())) {
                            handler.handle(Future.succeededFuture(found.result()));
                        } else {
                            handler.handle(Future.succeededFuture());
                        }

                    });
                }).end();
            } else {
                handler.handle(Future.succeededFuture());
            }
        });
    }

    private boolean isRemoved(JsonObject json) {
        return json.getBoolean(ID_FOUND);
    }

    @Override
    public void removeIfPresent(Key key, Value value, Handler<AsyncResult<Boolean>> handler) {
        context.vertx().createHttpClient().delete(9200, "localhost", entryPath(key), response -> {
            response.bodyHandler(body -> {

                if (isRemoved(body.toJsonObject())) {
                    handler.handle(Future.succeededFuture(true));
                } else {
                    handler.handle(Future.succeededFuture(false));
                }

            });
        }).end();
    }

    @Override
    public void replace(Key key, Value value, Handler<AsyncResult<Value>> handler) {
        get(key, get -> {
            put(key, value, replace -> {

                // todo add query parameters to get request to match the given doc!

                if (replace.succeeded() && get.result() != null) {
                    handler.handle(Future.succeededFuture(get.result()));
                } else {
                    handler.handle(Future.succeededFuture());
                }
            });
        });
    }

    @Override
    public void replaceIfPresent(Key key, Value oldValue, Value newValue, Handler<AsyncResult<Boolean>> handler) {
        handler.handle(Future.succeededFuture());
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
