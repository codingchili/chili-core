package com.codingchili.core.storage;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.*;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.*;
import org.elasticsearch.client.indices.*;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.query.*;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

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
    private StorageContext<Value> context;
    private RestHighLevelClient client;
    private String index;

    public ElasticMap(Future<AsyncStorage<Value>> future, StorageContext<Value> context) {
        this.context = context;
        this.index = constructIndexName(context);

        try {
            client = new RestHighLevelClient(
                    RestClient.builder(
                            new HttpHost(context.host(), context.port(), scheme(context))));

            createIndexIfNotExists().setHandler(done -> {
                if (done.succeeded()) {
                    future.complete(ElasticMap.this);
                } else {
                    future.fail(done.cause());
                }
            });
        } catch (Throwable e) {
            future.fail(e);
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
        Future<Void> future = Future.future();
        IndicesClient indices = client.indices();

        indices.existsAsync(new GetIndexRequest(index), RequestOptions.DEFAULT, new ActionListener<>() {
            @Override
            public void onResponse(Boolean exists) {
                if (!exists) {
                    CreateIndexRequest request = new CreateIndexRequest(index);
                    configureMapping(request);
                    configureSettings(request);

                    indices.createAsync(request, RequestOptions.DEFAULT, new ActionListener<>() {
                        @Override
                        public void onResponse(CreateIndexResponse createIndexResponse) {
                            future.complete();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            future.tryFail(e);
                        }
                    });
                } else {
                    future.complete();
                }
            }

            @Override
            public void onFailure(Exception e) {
                future.tryFail(e);
            }
        });
        return future;
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
        GetRequest request = new GetRequest()
                .index(index)
                .id(key);

        client.getAsync(request, RequestOptions.DEFAULT, new ActionListener<>() {
            @Override
            public void onResponse(GetResponse document) {
                if (document.isExists()) {
                    handler.handle(result(context.toValue(document.getSourceAsString())));
                } else {
                    handler.handle(error(new ValueMissingException(key)));
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (e.getCause() instanceof IndexNotFoundException) {
                    handler.handle(error(new ValueMissingException(key)));
                } else {
                    handler.handle(error(e));
                }
            }
        });
    }

    @Override
    public void put(Value value, Handler<AsyncResult<Void>> handler) {
        IndexRequest request = new IndexRequest()
                .index(index)
                .source(Serializer.buffer(value).getBytes(), XContentType.JSON)
                .id(value.getId());

        client.indexAsync(request, RequestOptions.DEFAULT, new ActionListener<>() {
            @Override
            public void onResponse(IndexResponse index) {
                handler.handle(result());
            }

            @Override
            public void onFailure(Exception e) {
                handler.handle(error(e));
            }
        });
    }

    @Override
    public void putIfAbsent(Value value, Handler<AsyncResult<Void>> handler) {
        IndexRequest request = new IndexRequest()
                .index(index)
                .source(Serializer.buffer(value).getBytes(), XContentType.JSON)
                .id(value.getId());

        client.indexAsync(request.opType(DocWriteRequest.OpType.CREATE), RequestOptions.DEFAULT, new ActionListener<>() {
            @Override
            public void onResponse(IndexResponse response) {
                if (response.getResult().equals(DocWriteResponse.Result.CREATED)) {
                    handler.handle(result());
                } else {
                    handler.handle(error(new ValueAlreadyPresentException(value.getId())));
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (e instanceof ElasticsearchStatusException) {
                    ElasticsearchStatusException es = (ElasticsearchStatusException) e;
                    if (es.status().equals(RestStatus.CONFLICT)) {
                        handler.handle(error(new ValueAlreadyPresentException(value.getId())));
                    }
                }
                handler.handle(error(e));
            }
        });
    }

    @Override
    public void remove(String key, Handler<AsyncResult<Void>> handler) {
        DeleteRequest request = new DeleteRequest()
                .index(index)
                .id(key);

        client.deleteAsync(request, RequestOptions.DEFAULT, new ActionListener<>() {
            @Override
            public void onResponse(DeleteResponse response) {
                if (response.getResult().equals(DocWriteResponse.Result.DELETED)) {
                    handler.handle(result());
                } else {
                    handler.handle(error(new NothingToRemoveException(key)));
                }
            }

            @Override
            public void onFailure(Exception e) {
                handler.handle(error(e));
            }
        });
    }

    @Override
    public void update(Value value, Handler<AsyncResult<Void>> handler) {
        UpdateRequest request = new UpdateRequest()
                .index(index)
                .doc(Serializer.buffer(value).getBytes(), XContentType.JSON)
                .id(value.getId());

        client.updateAsync(request, RequestOptions.DEFAULT, new ActionListener<>() {
            @Override
            public void onResponse(UpdateResponse response) {
                if (response.getResult().equals(DocWriteResponse.Result.UPDATED)) {
                    handler.handle(result());
                } else {
                    handler.handle(error(new NothingToUpdateException(value.getId())));
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (e instanceof ElasticsearchStatusException) {
                    ElasticsearchStatusException es = (ElasticsearchStatusException) e;
                    if (es.status().equals(RestStatus.NOT_FOUND)) {
                        handler.handle(error(new NothingToUpdateException(value.getId())));
                    } else {
                        handler.handle(error(e));
                    }
                } else {
                    handler.handle(error(e));
                }
            }
        });
    }

    @Override
    public void values(Handler<AsyncResult<Stream<Value>>> handler) {
        SearchRequest request = new SearchRequest()
                .indices(index)
                .source(new SearchSourceBuilder()
                        .query(QueryBuilders.matchAllQuery())
                        .size(MAX_RESULTS)
                        .fetchSource(true));


        client.searchAsync(request, RequestOptions.DEFAULT, new ActionListener<>() {
            @Override
            public void onResponse(SearchResponse search) {
                if (search.getHits() != null) {
                    handler.handle(result(StreamSupport.stream(search.getHits().spliterator(), false)
                            .map(source -> context.toValue(source.getSourceAsString()))));
                } else {
                    handler.handle(result(Stream.empty()));
                }
            }

            @Override
            public void onFailure(Exception e) {
                handler.handle(error(e));
            }
        });
    }

    @Override
    public void clear(Handler<AsyncResult<Void>> handler) {

        DeleteIndexRequest request = new DeleteIndexRequest(index);

        client.indices().deleteAsync(request, RequestOptions.DEFAULT, new ActionListener<>() {
            @Override
            public void onResponse(AcknowledgedResponse response) {
                if (response.isAcknowledged()) {
                    handler.handle(result());
                } else {
                    handler.handle(error(new StorageFailureException()));
                }
            }

            @Override
            public void onFailure(Exception e) {
                handler.handle(result());
                //handler.handle(error(e));
            }
        });
    }

    @Override
    public void size(Handler<AsyncResult<Integer>> handler) {
        SearchRequest request = new SearchRequest()
                .indices(index);

        SearchSourceBuilder source = new SearchSourceBuilder()
                .fetchSource(false)
                .size(0)
                .query(QueryBuilders.matchAllQuery());

        request.source(source);

        client.searchAsync(request, RequestOptions.DEFAULT, new ActionListener<>() {
            @Override
            public void onResponse(SearchResponse response) {
                if (response.status().equals(RestStatus.OK)) {
                    handler.handle(result((int) response.getHits().getTotalHits().value));
                } else {
                    handler.handle(result(0));
                }
            }

            @Override
            public void onFailure(Exception e) {
                handler.handle(error(e));
            }
        });
    }

    @Override
    public QueryBuilder<Value> query() {
        return new AbstractQueryBuilder<>(this, ARRAY_NOTATION) {
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

                SearchSourceBuilder source = getRequestWithOptions().query(query);
                SearchRequest request = new SearchRequest()
                        .indices(index)
                        .source(source);

                client.searchAsync(request, RequestOptions.DEFAULT, new ActionListener<SearchResponse>() {
                    @Override
                    public void onResponse(SearchResponse searchResponse) {
                        handler.handle(result(listFrom(searchResponse.getHits().getHits())));
                    }

                    @Override
                    public void onFailure(Exception e) {
                        handler.handle(error(e));
                    }
                });
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
