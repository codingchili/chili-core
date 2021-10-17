package com.codingchili.core.storage;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.*;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.query.*;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.Validator;
import com.codingchili.core.storage.exception.*;

import static com.codingchili.core.context.FutureHelper.*;

/**
 * Map implementation that uses ElasticSearch.
 * Does not support case sensitivity for equals.
 * Does not support ordering nested fields without server configuration
 */
public class ElasticMap<Value extends Storable> implements AsyncStorage<Value> {
    private static final String ID_MAPPINGS = "mappings";
    private static final String ID_SETTINGS = "settings";
    private static final int MAX_RESULTS = 10000;
    public static final String ARRAY_NOTATION = "";
    private final StorageContext<Value> context;
    private final String index;
    private RestHighLevelClient client;

    public ElasticMap(Promise<AsyncStorage<Value>> promise, StorageContext<Value> context) {
        this.context = context;
        this.index = constructIndexName(context);

        try {
            client = new RestHighLevelClient(
                    RestClient.builder(
                            new HttpHost(context.host(), context.port(), scheme(context))));

            createIndexIfNotExists().onComplete(done -> {
                if (done.succeeded()) {
                    promise.complete(ElasticMap.this);
                } else {
                    promise.fail(done.cause());
                }
            });
        } catch (Throwable e) {
            promise.fail(e);
        }
    }

    private String scheme(StorageContext<Value> context) {
        return context.storage().isSecure() ? "https" : "http";
    }

    private String constructIndexName(StorageContext<Value> context) {
        if (context.collection() != null) {
            return String.format("%s.%s",
                    context.database().toLowerCase(),
                    context.collection().toLowerCase());
        } else {
            return context.database();
        }
    }

    private Future<Void> createIndexIfNotExists() {
        Promise<Void> promise = Promise.promise();
        context.blocking((done) -> {
            IndicesClient indices = client.indices();
            try {
                var exists = indices.exists(new GetIndexRequest(index), RequestOptions.DEFAULT);
                if (!exists) {
                    var request = new CreateIndexRequest(index);
                    configureMapping(request);
                    configureSettings(request);
                    indices.create(request, RequestOptions.DEFAULT);
                }
                done.complete();
            } catch (Throwable e) {
                done.fail(e);
            }
        }, promise);
        return promise.future();
    }

    private void configureMapping(CreateIndexRequest request) {
        JsonObject properties = context.properties();
        if (properties.containsKey(ID_MAPPINGS)) {
            request.mapping(
                    properties.getJsonObject(ID_MAPPINGS).encodePrettily(),
                    XContentType.JSON
            );
        }
    }

    private void configureSettings(CreateIndexRequest request) {
        JsonObject properties = context.properties();
        if (properties.containsKey(ID_SETTINGS)) {
            request.settings(
                    properties.getJsonObject(ID_SETTINGS).encodePrettily(),
                    XContentType.JSON
            );
        }
    }

    @Override
    public void get(String key, Handler<AsyncResult<Value>> handler) {
        context.blocking((done) -> {
            GetRequest request = new GetRequest()
                    .index(index)
                    .id(key);
            try {
                var document = client.get(request, RequestOptions.DEFAULT);
                if (document.isExists()) {
                    done.handle(result(context.toValue(document.getSourceAsString())));
                } else {
                    done.handle(error(new ValueMissingException(key)));
                }
            } catch (Throwable e) {
                if (e instanceof IndexNotFoundException) {
                    done.fail(new ValueMissingException(key));
                } else {
                    done.fail(e);
                }
            }
        }, handler);
    }

    @Override
    public void put(Value value, Handler<AsyncResult<Void>> handler) {
        context.blocking(done -> {
            IndexRequest request = new IndexRequest()
                    .index(index)
                    .source(Serializer.buffer(value).getBytes(), XContentType.JSON)
                    .id(value.getId());
            try {
                client.index(request, RequestOptions.DEFAULT);
                done.complete();
            } catch (Throwable e) {
                done.fail(e);
            }
        }, handler);
    }

    @Override
    public void putIfAbsent(Value value, Handler<AsyncResult<Void>> handler) {
        context.blocking(done -> {
            IndexRequest request = new IndexRequest()
                    .index(index)
                    .source(Serializer.buffer(value).getBytes(), XContentType.JSON)
                    .id(value.getId())
                    .opType(DocWriteRequest.OpType.CREATE);
            try {
                var response = client.index(request, RequestOptions.DEFAULT);
                if (response.getResult().equals(DocWriteResponse.Result.CREATED)) {
                    done.complete();
                } else {
                    done.fail(new ValueAlreadyPresentException(value.getId()));
                }
            } catch (Throwable e) {
                if (matches(e, RestStatus.CONFLICT)) {
                    done.fail(new ValueAlreadyPresentException(value.getId()));
                } else {
                    done.fail(e);
                }
            }
        }, handler);
    }

    private boolean matches(Throwable e, RestStatus status) {
        if (e instanceof ElasticsearchStatusException) {
            return ((ElasticsearchStatusException) e).status().equals(status);
        } else {
            return false;
        }
    }

    @Override
    public void remove(String key, Handler<AsyncResult<Void>> handler) {
        context.blocking(done -> {
            DeleteRequest request = new DeleteRequest()
                    .index(index)
                    .id(key);
            try {
                var response = client.delete(request, RequestOptions.DEFAULT);

                if (response.getResult().equals(DocWriteResponse.Result.DELETED)) {
                    done.complete();
                } else {
                    done.fail(new NothingToRemoveException(key));
                }
            } catch (Throwable e) {
                done.fail(e);
            }
        }, handler);
    }

    @Override
    public void update(Value value, Handler<AsyncResult<Void>> handler) {
        context.blocking(done -> {
            UpdateRequest request = new UpdateRequest()
                    .index(index)
                    .doc(Serializer.buffer(value).getBytes(), XContentType.JSON)
                    .id(value.getId());
            try {
                var response = client.update(request, RequestOptions.DEFAULT);

                if (response.getResult().equals(DocWriteResponse.Result.UPDATED)) {
                    done.complete();
                } else {
                    done.fail(new NothingToUpdateException(value.getId()));
                }
            } catch (Throwable e) {
                if (matches(e, RestStatus.NOT_FOUND)) {
                    done.fail(new NothingToUpdateException(value.getId()));
                } else {
                    done.fail(e);
                }
            }
        }, handler);
    }

    @Override
    public void values(Handler<AsyncResult<Stream<Value>>> handler) {
        context.blocking(done -> {
            SearchRequest request = new SearchRequest()
                    .indices(index)
                    .source(new SearchSourceBuilder()
                            .query(QueryBuilders.matchAllQuery())
                            .size(MAX_RESULTS)
                            .fetchSource(true));

            try {
                var search = client.search(request, RequestOptions.DEFAULT);

                if (search.getHits() != null) {
                    done.complete(StreamSupport.stream(search.getHits().spliterator(), false)
                            .map(source -> context.toValue(source.getSourceAsString())));
                } else {
                    done.complete(Stream.empty());
                }
            } catch (Throwable e) {
                done.fail(e);
            }
        }, handler);
    }

    @Override
    public void clear(Handler<AsyncResult<Void>> handler) {
        context.blocking(done -> {
            DeleteIndexRequest request = new DeleteIndexRequest(index);
            try {
                var response = client.indices().delete(request, RequestOptions.DEFAULT);

                if (response.isAcknowledged()) {
                    done.complete();
                } else {
                    done.fail(new StorageFailureException());
                }
            } catch (Throwable e) {
                if (matches(e, RestStatus.NOT_FOUND)) {
                    // attempted to delete an index that does not exist should succeed.
                    done.complete();
                } else {
                    done.fail(e);
                }
            }
        }, handler);
    }

    @Override
    public void size(Handler<AsyncResult<Integer>> handler) {
        context.blocking(done -> {
            SearchRequest request = new SearchRequest()
                    .indices(index);

            SearchSourceBuilder source = new SearchSourceBuilder()
                    .fetchSource(false)
                    .size(0)
                    .query(QueryBuilders.matchAllQuery());

            request.source(source);
            try {
                var response = client.search(request, RequestOptions.DEFAULT);

                if (response.status().equals(RestStatus.OK)) {
                    done.complete((int) response.getHits().getTotalHits().value);
                } else {
                    done.complete(0);
                }
            } catch (Throwable e) {
                done.fail(e);
            }
        }, handler);
    }

    @Override
    public QueryBuilder<Value> query() {
        return new AbstractQueryBuilder<>(this, ARRAY_NOTATION) {
            final List<BoolQueryBuilder> statements = new ArrayList<>();
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
                context.blocking(done -> {
                    if (!builder.equals(new BoolQueryBuilder())) {
                        statements.add(builder);
                    }

                    BoolQueryBuilder query = new BoolQueryBuilder().minimumShouldMatch(1);
                    for (BoolQueryBuilder statement : statements) {
                        query.should(statement);
                    }

                    SearchSourceBuilder source = getRequestWithOptions().query(query);
                    SearchRequest request = new SearchRequest()
                            .indices(index)
                            .source(source);
                    try {
                        var response = client.search(request, RequestOptions.DEFAULT);
                        done.complete(listFrom(response.getHits().getHits()));
                    } catch (Throwable e) {
                        done.fail(e);
                    }
                }, handler);
            }

            private SearchSourceBuilder getRequestWithOptions() {
                SearchSourceBuilder source = new SearchSourceBuilder()
                        .size(MAX_RESULTS)
                        .fetchSource(true);

                if (isOrdered()) {
                    switch (getSortOrder()) {
                        case ASCENDING:
                            source.sort(new FieldSortBuilder(getOrderByAttribute()).order(SortOrder.ASC));
                            break;
                        case DESCENDING:
                            source.sort(new FieldSortBuilder(getOrderByAttribute()).order(SortOrder.DESC));
                    }
                }
                source.size(getPageSize());
                source.from(getPageSize() * getPage());
                return source;
            }
        };
    }

    @Override
    public StorageContext<Value> context() {
        return context;
    }

    @Override
    public void addIndex(String field) {
        // no-op: all fields are indexed.
    }

    private List<Value> listFrom(SearchHit[] hits) {
        List<Value> list = new ArrayList<>();

        for (SearchHit hit : hits) {
            BytesReference ref = hit.getSourceRef();
            if (ref != null) {
                list.add(context.toValue(ref.utf8ToString()));
            }
        }

        return list;
    }
}
