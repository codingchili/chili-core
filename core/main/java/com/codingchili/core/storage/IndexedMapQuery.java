package com.codingchili.core.storage;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.QueryFactory;
import com.googlecode.cqengine.query.option.AttributeOrder;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.resultset.ResultSet;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.googlecode.cqengine.query.QueryFactory.*;
import static com.googlecode.cqengine.query.option.DeduplicationStrategy.LOGICAL_ELIMINATION;

public class IndexedMapQuery<Value extends Storable> extends AbstractQueryBuilder<Value> {
    private Attribute<Value, String> field = fields.get(attribute);
    private List<Query<Value>> statements = new ArrayList<>();
    private Query<Value> builder;

    public IndexedMapQuery(IndexedMap<Value> storage, String attribute) {
        super(storage, attribute);
        prepareField(attribute);
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

    private QueryBuilder<Value> prepareField(String attribute) {
        setAttribute(attribute);
        field = createIndex(attribute(), isAttributeArray());
        return this;
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

    @Override
    public void execute(Handler<AsyncResult<Collection<Value>>> handler) {
        next();

        context.blocking(blocking -> {
            ResultSet<Value> values = db.retrieve(builder, getQueryOptions());
            blocking.complete(StreamSupport.stream(values.spliterator(), false)
                    .skip(pageSize * page)
                    .limit(pageSize)
                    .collect(Collectors.toList()));
            values.close();
        }, handler);
    }

    private QueryOptions getQueryOptions() {
        if (isOrdered) {
            createIndex(getOrderByAttribute(), false);
            AttributeOrder<Value> order;

            if (sortOrder.equals(SortOrder.ASCENDING)) {
                order = ascending(missingLast(fields.get(getOrderByAttribute())));
            } else {
                order = descending(missingLast(fields.get(getOrderByAttribute())));
            }
            return queryOptions(
                    QueryFactory.orderBy(order),
                    deduplicate(LOGICAL_ELIMINATION));
        } else {
            return queryOptions(QueryFactory.deduplicate(LOGICAL_ELIMINATION));
        }
    }
}
