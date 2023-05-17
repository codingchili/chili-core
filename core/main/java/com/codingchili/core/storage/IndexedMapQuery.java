package com.codingchili.core.storage;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.QueryFactory;
import com.googlecode.cqengine.query.option.*;
import com.googlecode.cqengine.resultset.ResultSet;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.googlecode.cqengine.query.QueryFactory.*;

/**
 * Common query class used for disk persisted storage and in-memory indexed.
 *
 * @param <Value> the value that is being queried.
 */
public class IndexedMapQuery<Value extends Storable> extends AbstractQueryBuilder<Value> {
    private Attribute<Value, String> field;
    private List<Query<Value>> statements = new ArrayList<>();
    private IndexedMap<Value> storage;
    private Query<Value> builder;

    public IndexedMapQuery(IndexedMap<Value> storage) {
        super(storage);
        this.storage = storage;
    }

    @Override
    public QueryBuilder<Value> on(String attribute) {
        prepareField(attribute);
        setAttribute(attribute);
        return this;
    }

    @Override
    public QueryBuilder<Value> and(String attribute) {
        prepareField(attribute);
        return this;
    }

    @Override
    public QueryBuilder<Value> or(String attribute) {
        next();
        prepareField(attribute);
        return this;
    }

    private void prepareField(String attribute) {
        setAttribute(attribute);
        field = storage.getAttribute(attribute);
    }

    private void next() {
        Query<Value> bucket;

        if (statements.size() >= 2) {
            bucket = QueryFactory.and(statements.remove(0), statements.remove(0), statements);
        } else if (statements.size() == 1) {
            bucket = statements.get(0);
        } else {
            return; // empty statement, ignore.
        }

        if (builder == null) {
            builder = bucket;
        } else {
            builder = QueryFactory.or(builder, bucket);
        }
        statements.clear();
    }

    @Override
    public QueryBuilder<Value> between(Long minimum, Long maximum) {
        statements.add(QueryFactory.between(field, minimum + "", maximum + ""));
        return this;
    }

    @Override
    public QueryBuilder<Value> like(String text) {
        statements.add(QueryFactory.contains(field, text));
        return this;
    }

    @Override
    public QueryBuilder<Value> startsWith(String text) {
        statements.add(QueryFactory.startsWith(field, text));
        return this;
    }

    @Override
    public QueryBuilder<Value> in(Comparable... list) {
        statements.add(QueryFactory.in(field,
                Arrays.stream(list)
                        .map(Object::toString)
                        .collect(Collectors.toList())));
        return this;
    }

    @Override
    public QueryBuilder<Value> equalTo(Comparable match) {
        statements.add(QueryFactory.equal(field, (match + "")));
        return this;
    }

    @Override
    public QueryBuilder<Value> matches(String regex) {
        statements.add(QueryFactory.matchesRegex(field, regex));
        return this;
    }

    private Function<Value, Value> mapper = (value) -> value;

    @Override
    public void execute(Handler<AsyncResult<Collection<Value>>> handler) {
        next();

        storage.context.blocking(blocking -> {
            try (ResultSet<Value> values = storage.db.retrieve(builder, getQueryOptions())) {
                blocking.complete(StreamSupport.stream(values.spliterator(), false)
                        .skip(getPageSize() * getPage())
                        .limit(getPageSize())
                        .map(mapper).collect(Collectors.toList()));
            } catch (Exception e) {
                blocking.fail(e);
            }
        }, handler);
    }

    public IndexedMapQuery<Value> setMapper(Function<Value, Value> mapper) {
        this.mapper = mapper;
        return this;
    }


    private QueryOptions getQueryOptions() {
        if (isOrdered()) {
            AttributeOrder<Value> order;

            // no need to support sorting on multivalued fields.
            if (getSortOrder().equals(SortOrder.ASCENDING)) {
                order = ascending(missingLast(storage.getAttribute(getOrderByAttribute())));
            } else {
                order = descending(missingLast(storage.getAttribute(getOrderByAttribute())));
            }
            return queryOptions(
                    QueryFactory.orderBy(order), deduplicate(DeduplicationStrategy.LOGICAL_ELIMINATION));
        } else {
            return queryOptions(deduplicate(DeduplicationStrategy.LOGICAL_ELIMINATION));
        }
    }
}
