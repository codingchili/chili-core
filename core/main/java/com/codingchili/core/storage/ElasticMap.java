package com.codingchili.core.storage;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.security.Validator;
import com.codingchili.core.storage.exception.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.engine.DocumentMissingException;
import org.elasticsearch.index.engine.VersionConflictEngineException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RegexpFlag;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static com.codingchili.core.context.FutureHelper.error;
import static com.codingchili.core.context.FutureHelper.result;

/**
 * @author Robin Duda
 * <p>
 * Map implementation that uses ElasticSearch.
 * Does not support case sensitivity for equals.
 * Does not support ordering nested fields without server configuration
 */
public class ElasticMap<Value extends Storable> implements AsyncStorage<Value> {
    private StorageContext<Value> context;
    private Logger logger;
    private TransportClient client;

    public ElasticMap(Future<AsyncStorage<Value>> future, StorageContext<Value> context) throws IOException {
        this.context = context;
        this.logger = context.logger(getClass());
        try {
            this.client = new PreBuiltTransportClient(Settings.builder()
                    .put("client.transport.sniff", true)
                    .build())
                    .addTransportAddress(
                            new InetSocketTransportAddress(InetAddress.getByName(context.host()), context.port()));

            client.admin().indices().create(new CreateIndexRequest(context.database())).get();
        } catch (UnknownHostException | InterruptedException | ExecutionException e) {
            logger.onError(e);
        }
        future.complete(this);
    }

    @Override
    public void get(String key, Handler<AsyncResult<Value>> handler) {
        client.prepareGet(context.database(), context.collection(), key)
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
    public void put(Value value, Handler<AsyncResult<Void>> handler) {
        client.prepareIndex(context.database(), context.collection(), value.id())
                .setSource(context.toJson(value).encode(), XContentType.JSON)
                .execute(new ElasticHandler<>(response -> {
                    handler.handle(result());
                }, exception -> handler.handle(error(exception))));
    }

    @Override
    public void putIfAbsent(Value value, Handler<AsyncResult<Void>> handler) {
        client.prepareIndex(context.database(), context.collection(), value.id())
                .setSource(context.toJson(value).encode(), XContentType.JSON)
                .setOpType(IndexRequest.OpType.CREATE)
                .execute(new ElasticHandler<>(response -> {

                    if (response.getResult().equals(DocWriteResponse.Result.CREATED)) {
                        handler.handle(result());
                    } else {
                        handler.handle(error(new ValueAlreadyPresentException(value.id())));
                    }
                }, exception -> {
                    if (nested(exception) instanceof VersionConflictEngineException) {
                        handler.handle(error(new ValueAlreadyPresentException(value.id())));
                    } else {
                        handler.handle(error(exception));
                    }
                }));
    }

    private Throwable nested(Throwable exception) {
        return exception.getCause().getCause();
    }

    @Override
    public void remove(String key, Handler<AsyncResult<Void>> handler) {
        client.prepareDelete(context.database(), context.collection(), key)
                .execute(new ElasticHandler<>(response -> {

                    if (response.getResult().equals(DocWriteResponse.Result.NOT_FOUND)) {
                        handler.handle(error(new NothingToRemoveException(key)));
                    } else {
                        handler.handle(result());
                    }
                }, exception -> handler.handle(error(exception))));
    }

    @Override
    public void update(Value value, Handler<AsyncResult<Void>> handler) {
        client.prepareUpdate(context.database(), context.collection(), value.id())
                .setDoc(context.toPacked(value), XContentType.JSON)
                .execute(new ElasticHandler<>(response -> {

                    if (response.getResult().ordinal() != 0) {
                        handler.handle(result());
                    } else {
                        handler.handle(error(new NothingToReplaceException(value.id())));
                    }
                }, exception -> {
                    if (nested(exception) instanceof DocumentMissingException) {
                        handler.handle(error(new NothingToReplaceException(value.id())));
                    } else {
                        handler.handle(error(exception));
                    }
                }));
    }

    @Override
    public void values(Handler<AsyncResult<Collection<Value>>> handler) {
        client.prepareSearch(context.database()).setTypes(context.collection())
                .setFetchSource(true)
                .setSize(Integer.MAX_VALUE)
                .setQuery(QueryBuilders.matchAllQuery())
                .execute(new ElasticHandler<>(response -> {
                    if (response.status().equals(RestStatus.OK)) {
                        handler.handle(result(listFrom(response.getHits().getHits())));
                    } else {
                        // no items in map -> empty list back.
                        handler.handle(result(new ArrayList<>()));
                    }
                }, exception -> handler.handle(error(exception))));
    }

    @Override
    public void clear(Handler<AsyncResult<Void>> handler) {
        DeleteIndexResponse response = client.admin()
                .indices()
                .delete(new DeleteIndexRequest(context.database()))
                .actionGet();

        if (response.isAcknowledged()) {
            client.admin().indices().refresh(new RefreshRequest(context.database()));
            handler.handle(result());
            context.onCollectionDropped();
        } else {
            handler.handle(error(new StorageFailureException()));
        }
    }

    @Override
    public void size(Handler<AsyncResult<Integer>> handler) {
        client.prepareSearch(context.database()).setTypes(context.collection())
                .setFetchSource(false)
                .setSize(0)
                .setQuery(QueryBuilders.matchAllQuery())
                .execute(new ElasticHandler<>(response -> {
                    if (response.status().equals(RestStatus.OK)) {
                        handler.handle(result((int) response.getHits().getTotalHits()));
                    } else {
                        handler.handle(result(0));
                    }
                }, exception -> handler.handle(error(exception))));
    }

    @Override
    public QueryBuilder<Value> query(String field) {
        return new AbstractQueryBuilder<Value>(this, field) {
            List<BoolQueryBuilder> statements = new ArrayList<>();
            BoolQueryBuilder builder = new BoolQueryBuilder();

            @Override
            public QueryBuilder<Value> and(String attribute) {
                setAttribute(attribute);
                return this;
            }

            @Override
            public QueryBuilder<Value> or(String attribute) {
                setAttribute(attribute);
                statements.add(builder);
                builder = new BoolQueryBuilder();
                return this;
            }

            @Override
            public QueryBuilder<Value> between(Long minimum, Long maximum) {
                builder.must(QueryBuilders.rangeQuery(attribute()).gte(minimum).lte(maximum));
                return this;
            }

            @Override
            public QueryBuilder<Value> like(String text) {
                text = Validator.toPlainText(text).toLowerCase();
                builder.must(QueryBuilders.wildcardQuery(attribute(), "*" + text + "*"));
                return this;
            }

            @Override
            public QueryBuilder<Value> startsWith(String text) {
                builder.must(QueryBuilders.matchPhrasePrefixQuery(attribute(), text));
                return this;
            }

            @Override
            public QueryBuilder<Value> in(Comparable... list) {
                BoolQueryBuilder bool = new BoolQueryBuilder().minimumShouldMatch(1);
                for (Comparable item : list) {
                    bool.should(QueryBuilders.matchPhraseQuery(attribute(), item));
                }
                builder.must(bool);
                return this;
            }

            @Override
            public QueryBuilder<Value> equalTo(Comparable match) {
                builder.must(QueryBuilders.matchPhraseQuery(attribute(), match));
                return this;
            }

            @Override
            public QueryBuilder<Value> matches(String regex) {
                if (regex.contains("^") || regex.contains("$")) {
                    // remove unsupported characters in ElasticSearch query.
                    regex = regex.replaceAll("[\\^$]", "");
                }
                builder.must(QueryBuilders.regexpQuery(attribute(), regex.toLowerCase()).flags(RegexpFlag.ALL));
                return this;
            }

            @Override
            public void execute(Handler<AsyncResult<Collection<Value>>> handler) {
                if (!builder.equals(new BoolQueryBuilder())) {
                    statements.add(builder);
                }

                BoolQueryBuilder query = new BoolQueryBuilder().minimumShouldMatch(1);
                for (BoolQueryBuilder statement : statements) {
                    query.should(statement);
                }

                getRequestWithOptions().setQuery(query).execute(new ElasticHandler<>(response -> {
                    handler.handle(result(listFrom(response.getHits().getHits())));
                }, exception -> handler.handle(error(exception))));
            }

            private SearchRequestBuilder getRequestWithOptions() {
                SearchRequestBuilder request = client.prepareSearch(context.database()).setTypes(context.collection());
                if (isOrdered) {
                    switch (sortOrder) {
                        case ASCENDING:
                            request.addSort(getOrderByAttribute(), SortOrder.ASC);
                            break;
                        case DESCENDING:
                            request.addSort(getOrderByAttribute(), SortOrder.DESC);
                    }
                }
                request.setSize(pageSize);
                request.setFrom(pageSize * page);
                return request;
            }
        };
    }

    @Override
    public StorageContext<Value> context() {
        return context;
    }

    private Value valueFrom(byte[] value) {
        return context.toValue(value);
    }

    private List<Value> listFrom(SearchHit[] hits) {
        List<Value> list = new ArrayList<>();

        for (SearchHit hit : hits) {
            BytesReference ref = hit.getSourceRef();
            if (ref != null) {
                list.add(valueFrom(BytesReference.toBytes(ref)));
            }
        }

        return list;
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
