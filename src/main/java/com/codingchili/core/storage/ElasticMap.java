package com.codingchili.core.storage;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.storage.exception.*;

import static com.codingchili.core.context.FutureHelper.failed;
import static com.codingchili.core.context.FutureHelper.succeeded;

/**
 * @author Robin Duda
 *         <p>
 *         Mocks an async map used by Hazelcast to enable testing of storage logic.
 */
public class ElasticMap<Key, Value> implements AsyncStorage<Key, Value> {
    private StorageContext<Value> context;
    private TransportClient client;

    public ElasticMap(Future<AsyncStorage<Key, Value>> future, StorageContext<Value> context) {
        this.context = context;

        try {
            this.client = new PreBuiltTransportClient(Settings.builder()
                    .put("client.transport.sniff", true)
                    .build())
                    .addTransportAddress(
                            new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));

            client.admin().indices().create(new CreateIndexRequest(context.DB())).get();
        } catch (UnknownHostException | InterruptedException | ExecutionException e) {
            context.console().onError(e);
        }
        future.complete(this);
    }

    @Override
    public void get(Key key, Handler<AsyncResult<Value>> handler) {
        client.prepareGet(context.DB(), context.collection(), key.toString())
                .execute(new ElasticHandler<>(response -> {

                    if (response.isExists() && !response.isSourceEmpty()) {
                        handler.handle(succeeded(valueFrom(response.getSourceAsBytes())));
                    } else {
                        handler.handle(failed(new MissingEntityException(key)));
                    }
                }, exception -> handler.handle(failed(exception))));
    }

    @Override
    public void put(Key key, Value value, Handler<AsyncResult<Void>> handler) {
        put(key, value, Long.MAX_VALUE, handler);
    }

    @Override
    public void put(Key key, Value value, long ttl, Handler<AsyncResult<Void>> handler) {
        client.prepareIndex(context.DB(), context.collection(), key.toString())
                .setSource(context.toPacked(value))
                .setTTL(ttl)
                .execute(new DefaultHandler<>(handler));
    }

    @Override
    public void putIfAbsent(Key key, Value value, Handler<AsyncResult<Void>> handler) {
        putIfAbsent(key, value, Long.MAX_VALUE, handler);
    }

    @Override
    public void putIfAbsent(Key key, Value value, long ttl, Handler<AsyncResult<Void>> handler) {
        client.prepareIndex(context.DB(), context.collection(), key.toString())
                .setSource(context.toPacked(value))
                .setOpType(IndexRequest.OpType.CREATE)
                .setTTL(ttl)
                .execute(new ElasticHandler<>(response -> {

                    if (response.getResult().equals(DocWriteResponse.Result.CREATED)) {
                        handler.handle(succeeded());
                    } else {
                        handler.handle(failed(new ValueAlreadyPresentException(key)));
                    }
                }, exception -> handler.handle(failed(exception))));
    }

    @Override
    public void remove(Key key, Handler<AsyncResult<Void>> handler) {
        client.prepareDelete(context.DB(), context.collection(), key.toString())
                .execute(new ElasticHandler<>(response -> {

                    if (response.getResult().ordinal() != 0) {
                        handler.handle(succeeded());
                    } else {
                        handler.handle(failed(new NothingToRemoveException(key)));
                    }
                }, exception -> handler.handle(failed(exception))));
    }

    @Override
    public void replace(Key key, Value value, Handler<AsyncResult<Void>> handler) {
        client.prepareUpdate(context.DB(), context.collection(), key.toString())
                .setDoc(context.toJson(value))
                .execute(new ElasticHandler<>(response -> {

                    if (response.getResult().ordinal() != 0) {
                        handler.handle(succeeded());
                    } else {
                        handler.handle(failed(new NothingToReplaceException(key)));
                    }
                }, exception -> handler.handle(failed(exception))));
    }

    @Override
    public void clear(Handler<AsyncResult<Void>> handler) {
        DeleteIndexResponse response = client.admin()
                .indices()
                .delete(new DeleteIndexRequest(context.DB()))
                .actionGet();

        if (response.isAcknowledged()) {
            client.admin().indices().refresh(new RefreshRequest(context.DB()));
            handler.handle(succeeded());
        } else {
            handler.handle(failed(new StorageFailureException()));
        }
    }

    @Override
    public void size(Handler<AsyncResult<Integer>> handler) {
        SearchResponse response = client.prepareSearch(context.DB())
                .setFetchSource(false)
                .setQuery(QueryBuilders.matchAllQuery())
                .get();

        if (response.status().equals(RestStatus.OK)) {
            handler.handle(succeeded((int) response.getHits().totalHits()));
        } else {
            handler.handle(failed(new StorageFailureException()));
        }
    }

    private Value valueFrom(byte[] value) {
        return context.toValue(value);
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

    /**
     * Simplifies the interface to ActionListener when using the ElasticSearch client.
     */
    class ElasticHandler<Response> implements ActionListener<Response> {
        private Consumer<Response> success;
        private Consumer<Exception> error;

        ElasticHandler(Consumer<Response> success, Consumer<Exception> error) {
            this.success = success;
            this.error = error;
        }

        @Override
        public void onResponse(Response response) {
            success.accept(response);
        }

        @Override
        public void onFailure(Exception e) {
            error.accept(e);
        }
    }

    /**
     * Performs default behavior on a future:
     * on success -> complete,
     * on error -> fail
     */
    private class DefaultHandler<Response, Result> extends ElasticHandler<Response> {
        DefaultHandler(Handler<AsyncResult<Result>> handler) {
            super(response -> handler.handle(succeeded()), exception -> handler.handle(failed(exception)));
        }
    }


}
