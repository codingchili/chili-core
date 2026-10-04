package com.codingchili.core.storage;

import io.vertx.core.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.codingchili.core.context.FutureHelper;
import com.codingchili.core.context.StorageContext;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.Validator;
import com.codingchili.core.storage.exception.*;

import static com.codingchili.core.configuration.CoreStrings.STORAGE_ARRAY;
import static com.codingchili.core.context.FutureHelper.*;

/**
 * mongodb backed asyncmap.
 */
public class MongoDBMap<Value extends Storable> implements AsyncStorage<Value> {
    private static final JsonObject ALL_FIELDS = new JsonObject();
    private static final String ID = "_id";
    private static final String AND = "$and";
    private static final String OR = "$or";
    private static final String GTE = "$gte";
    private static final String LTE = "$lte";
    private static final String REGEX = "$regex";
    private static final String IN = "$in";
    private static final String OPTIONS = "$options";
    private Set<String> indexed = ConcurrentHashMap.newKeySet();
    private StorageContext<Value> context;
    private MongoClient client;
    private String collection;

    public MongoDBMap(Promise<AsyncStorage<Value>> promise, StorageContext<Value> context) {
        client = MongoClient.createShared(context.vertx(), Serializer.json(context.storage()));

        this.collection = context.collection();
        this.context = context;

        createIndex(ID).onComplete(done -> promise.complete(this));
    }

    @Override
    public Future<Value> get(String key) {
        return client.findOne(collection, id(key), ALL_FIELDS).compose(document -> {
            if (document != null) {
                return result(context.toValue(document));
            } else {
                return error(new ValueMissingException(key));
            }
        });
    }

    @Override
    public Future<Void> put(Value value) {
        return client.replaceDocumentsWithOptions(collection, id(value.getId()), document(value),
                new UpdateOptions().setUpsert(true))
                .mapEmpty();
    }

    private JsonObject document(Value value) {
        return context.toJson(value).put(ID, value.getId());
    }

    @Override
    public Future<Void> putIfAbsent(Value value) {
        return client.insert(collection, document(value)).transform(put -> {
            if (put.succeeded()) {
                return FutureHelper.result();
            } else {
                return error(new ValueAlreadyPresentException(value.getId()));
            }
        });
    }

    @Override
    public Future<Void> remove(String key) {
        return client.removeDocument(collection, id(key)).compose(remove -> {
            if (remove.getRemovedCount() > 0) {
                return FutureHelper.result();
            } else {
                return error(new NothingToRemoveException(key));
            }
        });
    }

    private JsonObject id(String key) {
        return new JsonObject().put(ID, key);
    }

    private JsonObject id(Value value) {
        return id(value.getId());
    }

    @Override
    public Future<Void> update(Value value) {
        return client.replaceDocuments(collection, id(value), document(value)).compose(replace -> {
            if (replace.getDocModified() > 0) {
                return FutureHelper.result();
            } else {
                return error(new NothingToUpdateException(value.getId()));
            }
        });
    }

    @Override
    public Future<Stream<Value>> values() {
        return client.find(collection, new JsonObject())
                .map(found -> found.stream().map(json -> context.toValue(json)));
    }

    @Override
    public Future<Void> clear() {
        return client.dropCollection(collection);
    }

    @Override
    public Future<Integer> size() {
        return client.count(collection, new JsonObject()).map(Long::intValue);
    }

    @Override
    public void addIndex(String field) {
        createIndex(field);
    }

    private Future<Void> createIndex(String field) {
        if (!indexed.contains(field)) {
            indexed.add(field);
            field = field.replace(STORAGE_ARRAY, "");

            return client.createIndex(context.collection(), new JsonObject().put(field, ""));
        } else {
            return FutureHelper.result();
        }
    }

    @Override
    public QueryBuilder<Value> query() {
        return new AbstractQueryBuilder<>(this) {
            JsonArray statements = new JsonArray();
            JsonArray builder = new JsonArray();

            @Override
            public QueryBuilder<Value> on(String attribute) {
                setAttribute(attribute);
                return this;
            }

            @Override
            public QueryBuilder<Value> and(String attribute) {
                setAttribute(attribute);
                return this;
            }

            @Override
            public QueryBuilder<Value> or(String attribute) {
                setAttribute(attribute);
                apply();
                return this;
            }

            /**
             * Applies the current state of the builder to the final query.
             */
            private void apply() {
                statements.add(new JsonObject().put(AND, builder));
                builder = new JsonArray();
            }

            @Override
            public QueryBuilder<Value> between(Long minimum, Long maximum) {
                builder.add(new JsonObject()
                    .put(attribute(), new JsonObject()
                        .put(GTE, minimum)
                        .put(LTE, maximum)));
                return this;
            }

            @Override
            public QueryBuilder<Value> like(String text) {
                text = Validator.toPlainText(text);
                builder.add(new JsonObject()
                    .put(attribute(), new JsonObject()
                        .put(REGEX, "^.*" + text + ".*$")
                        .put(OPTIONS, "i")));
                return this;
            }

            @Override
            public QueryBuilder<Value> startsWith(String text) {
                text = Validator.toPlainText(text);
                builder.add(new JsonObject()
                    .put(attribute(), new JsonObject()
                        .put(REGEX, "^" + text + ".*")));
                return this;
            }

            @Override
            public QueryBuilder<Value> in(Comparable... comparables) {
                List<Comparable> list = new ArrayList<>(Arrays.asList(comparables));

                builder.add(new JsonObject()
                    .put(attribute(), new JsonObject()
                        .put(IN, list)));
                return this;
            }

            @Override
            public QueryBuilder<Value> equalTo(Comparable match) {
                builder.add(new JsonObject().put(attribute(), match));
                return this;
            }

            @Override
            public QueryBuilder<Value> matches(String regex) {
                builder.add(new JsonObject()
                    .put(attribute(), new JsonObject()
                        .put(REGEX, regex)));
                return this;
            }

            @Override
            public Future<Collection<Value>> execute() {
                apply();

                return client.findWithOptions(collection, new JsonObject().put(OR, statements), getOptions())
                        .map(found -> toList(found));
            }

            private FindOptions getOptions() {
                return new FindOptions()
                    .setLimit(getPageSize())
                    .setSkip(getPageSize() * getPage())
                    .setSort(getSortOptions());
            }

            private JsonObject getSortOptions() {
                if (isOrdered()) {
                    return new JsonObject().put(getOrderByAttribute(), getSortDirection());
                } else {
                    return new JsonObject();
                }
            }
        };
    }

    @Override
    public StorageContext<Value> context() {
        return context;
    }

    private List<Value> toList(Collection<JsonObject> results) {
        return results.stream().map(json -> context.toValue(json)).collect(Collectors.toList());
    }
}
