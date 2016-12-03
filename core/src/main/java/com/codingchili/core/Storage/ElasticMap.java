package com.codingchili.core.storage;

import io.vertx.core.*;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.engine.DocumentMissingException;
import org.elasticsearch.index.engine.VersionConflictEngineException;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.storage.exception.*;

import static com.codingchili.core.context.FutureHelper.*;

/**
 * @author Robin Duda
 *         <p>
 *         Mocks an async map used by Hazelcast to enable testing of storage logic.
 */
public class ElasticMap<Key, Value> implements AsyncStorage<Key, Value> {
    private StorageContext<Value> context;
    private TransportClient client;

    public ElasticMap(Future<AsyncStorage<Key, Value>> future, StorageContext<Value> context) throws IOException {
        this.context = context;
        try {
            this.client = new PreBuiltTransportClient(Settings.builder()
                    .put("client.transport.sniff", true)
                    .build())
                    .addTransportAddress(
                            new InetSocketTransportAddress(InetAddress.getByName(context.host()), context.port()));

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
                        handler.handle(result(valueFrom(response.getSourceAsBytes())));
                    } else {
                        handler.handle(error(new ValueMissingException(key)));
                    }
                }, exception -> {
                    if (exception.getCause() instanceof IndexNotFoundException) {
                        handler.handle(error(new ValueMissingException(key)));
                    } else {
                        handler.handle(error(exception));
                    }
                }));
    }

    @Override
    public void put(Key key, Value value, Handler<AsyncResult<Void>> handler) {
        client.prepareIndex(context.DB(), context.collection(), key.toString())
                .setSource(context.toJson(value).encode())
                .execute(new ElasticHandler<>(response -> {
                    handler.handle(result());
                }, exception -> handler.handle(error(exception))));
    }

    @Override
    public void put(Key key, Value value, long ttl, Handler<AsyncResult<Void>> handler) {
        put(key, value, result -> {
            if (result.succeeded()) {
                context.timer(ttl, event -> remove(key, removed -> {
                }));
                handler.handle(result());
            } else {
                handler.handle(error(result.cause()));
            }
        });
    }

    @Override
    public void putIfAbsent(Key key, Value value, Handler<AsyncResult<Void>> handler) {
        client.prepareIndex(context.DB(), context.collection(), key.toString())
                .setSource(context.toJson(value).encode())
                .setOpType(IndexRequest.OpType.CREATE)
                .execute(new ElasticHandler<>(response -> {

                    if (response.getResult().equals(DocWriteResponse.Result.CREATED)) {
                        handler.handle(result());
                    } else {
                        handler.handle(error(new ValueAlreadyPresentException(key)));
                    }
                }, exception -> {
                    if (nested(exception) instanceof VersionConflictEngineException) {
                        handler.handle(error(new ValueAlreadyPresentException(key)));
                    } else {
                        handler.handle(error(exception));
                    }
                }));
    }

    @Override
    public void putIfAbsent(Key key, Value value, long ttl, Handler<AsyncResult<Void>> handler) {
        putIfAbsent(key, value, result -> {
            if (result.succeeded()) {
                context.timer(ttl, event -> remove(key, removed -> {
                }));
                handler.handle(result());
            } else {
                handler.handle(error(result.cause()));
            }
        });
    }

    private Throwable nested(Throwable exception) {
        return exception.getCause().getCause();
    }

    @Override
    public void remove(Key key, Handler<AsyncResult<Void>> handler) {
        client.prepareDelete(context.DB(), context.collection(), key.toString())
                .execute(new ElasticHandler<>(response -> {

                    if (response.getResult().equals(DocWriteResponse.Result.NOT_FOUND)) {
                        handler.handle(error(new NothingToRemoveException(key)));
                    } else {
                        handler.handle(result());
                    }
                }, exception -> handler.handle(error(exception))));
    }

    @Override
    public void replace(Key key, Value value, Handler<AsyncResult<Void>> handler) {
        client.prepareUpdate(context.DB(), context.collection(), key.toString())
                .setDoc(context.toPacked(value))
                .execute(new ElasticHandler<>(response -> {

                    if (response.getResult().ordinal() != 0) {
                        handler.handle(result());
                    } else {
                        handler.handle(error(new NothingToReplaceException(key)));
                    }
                }, exception -> {
                    if (nested(exception) instanceof DocumentMissingException) {
                        handler.handle(error(new NothingToReplaceException(key)));
                    } else {
                        handler.handle(error(exception));
                    }
                }));
    }

    @Override
    public void clear(Handler<AsyncResult<Void>> handler) {
        DeleteIndexResponse response = client.admin()
                .indices()
                .delete(new DeleteIndexRequest(context.DB()))
                .actionGet();

        if (response.isAcknowledged()) {
            client.admin().indices().refresh(new RefreshRequest(context.DB()));
            handler.handle(result());
            context.onCollectionDropped();
        } else {
            handler.handle(error(new StorageFailureException()));
        }
    }

    @Override
    public void size(Handler<AsyncResult<Integer>> handler) {
        client.prepareSearch(context.DB())
                .setFetchSource(false)
                .setQuery(QueryBuilders.matchAllQuery())
                .execute(new ElasticHandler<>(response -> {
                    if (response.status().equals(RestStatus.OK)) {
                        handler.handle(result((int) response.getHits().totalHits()));
                    } else {
                        handler.handle(result(0));
                    }
                }, exception -> handler.handle(result(0))));
    }

    private Value valueFrom(byte[] value) {
        return context.toValue(value);
    }

    @Override
    public void queryExact(String attribute, Comparable comparable, Handler<AsyncResult<Collection<Value>>> handler) {
        client.prepareSearch(context.DB())
                .setTypes(context.collection())
                .setQuery(QueryBuilders.matchQuery(attribute, comparable))
                .execute(new ElasticHandler<>(response -> {
                    handler.handle(result(listFrom(response.getHits().getHits())));
                }, exception -> handler.handle(error(exception))));
    }

    private Collection<Value> listFrom(SearchHit[] hits) {
        List<Value> list = new ArrayList<>();

        for (SearchHit hit : hits) {
            list.add(valueFrom(hit.source()));
        }

        return list;
    }

    @Override
    public void querySimilar(String attribute, Comparable comparable, Handler<AsyncResult<Collection<Value>>> handler) {
        if (context.validate(comparable)) {
            client.prepareSearch(context.DB())
                    .setTypes(context.collection())
                    .setQuery(QueryBuilders.matchPhrasePrefixQuery(attribute, comparable))
                    .execute(new ElasticHandler<>(response -> {
                        handler.handle(result(listFrom(response.getHits().getHits())));
                    }, exception -> handler.handle(error(exception))));
        } else {
            handler.handle(result(new ArrayList<>()));
        }
    }

    @Override
    public void queryRange(String attribute, int from, int to, Handler<AsyncResult<Collection<Value>>> handler) {
        client.prepareSearch(context.DB())
                .setTypes(context.collection())
                .setQuery(QueryBuilders.rangeQuery(attribute).gte(from).lte(to))
                .execute(new ElasticHandler<>(response -> {
                    handler.handle(result(listFrom(response.getHits().getHits())));
                }, exception -> handler.handle(error(exception))));
    }

    /**
     * Simplifies the interface to ActionListener when using the ElasticSearch client.
     */
    private class ElasticHandler<Response> implements ActionListener<Response> {
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
}
