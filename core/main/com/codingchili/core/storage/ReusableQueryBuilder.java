package com.codingchili.core.storage;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.*;

import static com.codingchili.core.configuration.CoreStrings.STORAGE_ARRAY;

/**
 * @author Robin Duda
 *         <p>
 *         Create a storable and reusable statement.
 */
public class ReusableQueryBuilder<Value> implements QueryBuilder<Value> {
    private List<JsonArray> query = new ArrayList<>();
    private JsonObject options = new JsonObject();
    private String attribute;

    public ReusableQueryBuilder(String attribute) {
        query.add(new JsonArray());
        this.attribute = attribute;
    }

    @Override
    public ReusableQueryBuilder<Value> and(String attribute) {
        this.attribute = attribute;
        return this;
    }

    @Override
    public ReusableQueryBuilder<Value> or(String attribute) {
        next();
        this.attribute = attribute;
        return this;
    }

    private void next() {
        query.add(new JsonArray());
    }

    private JsonArray current() {
        return query.get(query.size() - 1);
    }

    @Override
    public ReusableQueryBuilder<Value> page(int page) {
        options.put("page", page);
        return this;
    }

    @Override
    public ReusableQueryBuilder<Value> pageSize(int pageSize) {
        options.put("pageSize", pageSize);
        return this;
    }

    @Override
    public ReusableQueryBuilder<Value> between(Long minimum, Long maximum) {
        add(new JsonObject().put("between", new JsonObject()
                .put("minimum", minimum)
                .put("maximum", maximum)));
        return this;
    }

    private void add(JsonObject clause) {
        current().add(new JsonObject().put(attribute, clause));
    }

    @Override
    public ReusableQueryBuilder<Value> like(String text) {
        add(new JsonObject().put("like", text));
        return this;
    }

    @Override
    public ReusableQueryBuilder<Value> startsWith(String text) {
        add(new JsonObject().put("startsWith", text));
        return this;
    }

    @Override
    public ReusableQueryBuilder<Value> in(Comparable... list) {
        List<String> comparables = new ArrayList<>();

        for (Comparable comparable : list) {
            comparables.add(comparable.toString());
        }
        add(new JsonObject().put("in", comparables));
        return this;
    }

    @Override
    public ReusableQueryBuilder<Value> equalTo(Comparable match) {
        add(new JsonObject().put("equalTo", match));
        return this;
    }

    @Override
    public ReusableQueryBuilder<Value> matches(String regex) {
        add(new JsonObject().put("matches", regex));
        return this;
    }

    @Override
    public ReusableQueryBuilder<Value> orderBy(String orderByAttribute) {
        options.put("orderBy", orderByAttribute);
        return this;
    }

    @Override
    public ReusableQueryBuilder<Value> order(SortOrder order) {
        options.put("order", order);
        return this;
    }

    @Override
    public String attribute() {
        return attribute;
    }

    @Override
    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    @Override
    public boolean isAttributeArray() {
        return attribute.contains(STORAGE_ARRAY);
    }

    @Override
    public String getOrderByAttribute() {
        return options.getString("orderBy");
    }

    @Override
    public void execute(Handler<AsyncResult<List<Value>>> handler) {
        throw new UnsupportedOperationException();
    }

    public JsonObject compile() {
        return new JsonObject()
                .put("query", query)
                .put("options", options);
    }

    /*public List<Value> evaluate(JsonObject query, QueryBuilder<Value> builder) {
        //parseQueury(query, builder);
        parseSettings(query, builder);
    }*/

    public void parseSettings(JsonObject query, QueryBuilder<Value> builder) {
        for (String option : query.getJsonObject("options").fieldNames()) {
            switch (option) {
                case "pageSize":
                    builder.pageSize(query.getInteger(option));
                    break;
                case "page":
                    builder.page(query.getInteger(option));
                    break;
                case "orderBy":
                    builder.orderBy(query.getString(option));
                    break;
                case "order":
                    builder.order(SortOrder.valueOf(query.getString(option)));
                    break;
            }
        }
    }
}
