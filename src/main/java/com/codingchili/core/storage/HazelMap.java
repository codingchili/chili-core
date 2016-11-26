package com.codingchili.core.storage;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.AsyncMap;

import java.util.List;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.storage.exception.*;

import static com.codingchili.core.context.FutureHelper.failed;
import static com.codingchili.core.context.FutureHelper.succeeded;

/**
 * @author Robin Duda
 *         <p>
 *         Initializes a new hazel async map.
 */
public class HazelMap<Key, Value> implements AsyncStorage<Key, Value> {
    private AsyncMap<Key, Value> map;

    /**
     * Initializes a new hazel async map.
     *
     * @param context the context requesting the map to be created.
     * @param future  called when the map is created.
     */
    public HazelMap(Future<AsyncStorage> future, StorageContext context) {
        context.vertx().sharedData().<Key, Value>getClusterWideMap(context.DB(), cluster -> {
            if (cluster.succeeded()) {
                this.map = cluster.result();
                future.complete(this);
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
                    handler.handle(succeeded(get.result()));
                } else {
                    handler.handle(failed(new MissingEntityException(key)));
                }
            } else {
                handler.handle(failed(get.cause()));
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
        putIfAbsent(key, value, Long.MAX_VALUE, handler);
    }

    @Override
    public void putIfAbsent(Key key, Value value, long ttl, Handler<AsyncResult<Void>> handler) {
        map.putIfAbsent(key, value, ttl, put -> {
            if (put.succeeded()) {
                if (put.result() == null) {
                    handler.handle(succeeded());
                } else {
                    handler.handle(failed(new ValueAlreadyPresentException(key)));
                }
            } else {
                handler.handle(failed(put.cause()));
            }
        });
    }

    @Override
    public void remove(Key key, Handler<AsyncResult<Void>> handler) {
        map.remove(key, remove -> {
            if (remove.succeeded()) {
                if (remove.result() == null) {
                    handler.handle(failed(new NothingToRemoveException(key)));
                } else {
                    handler.handle(succeeded());
                }
            } else {
                handler.handle(failed(remove.cause()));
            }
        });
    }

    @Override
    public void replace(Key key, Value value, Handler<AsyncResult<Void>> handler) {
        map.replace(key, value, replace -> {
            if (replace.succeeded()) {
                if (replace.result() == null) {
                    handler.handle(failed(new NothingToReplaceException(key)));
                } else {
                    handler.handle(succeeded());
                }
            } else {
                handler.handle(failed(replace.cause()));
            }
        });
    }

    @Override
    public void clear(Handler<AsyncResult<Void>> handler) {
        map.clear(clear -> {
            if (clear.succeeded()) {
                handler.handle(succeeded());
            } else {
                handler.handle(failed(clear.cause()));
            }
        });
    }

    @Override
    public void size(Handler<AsyncResult<Integer>> handler) {
        map.size(size -> {
            if (size.succeeded()) {
                handler.handle(succeeded(size.result()));
            } else {
                handler.handle(failed(size.cause()));
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
