package com.codingchili.core.storage;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.TimerSource;
import com.codingchili.core.storage.exception.QueryFormatException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.*;
import java.util.function.Consumer;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * @author Robin Duda
 *         <p>
 *         Create a storable and reusable query.
 */
public class ReusableQueryBuilder<Value extends Storable> implements QueryBuilder<Value> {
    private static final String PAGE_SIZE = "pageSize";
    private static final String PAGE = "page";
    private static final String ORDER_BY = "orderBy";
    private static final String ORDER = "order";
    private static final String MATCHES = "matches";
    private static final String EQUAL_TO = "equalTo";
    private static final String IN = "in";
    private static final String STARTS_WITH = "startsWith";
    private static final String LIKE = "like";
    private static final String MINIMUM = "minimum";
    private static final String MAXIMUM = "maximum";
    private static final String BETWEEN = "between";
    private AsyncStorage<Value> storage;
    private JsonArray query = new JsonArray();
    private JsonObject options = new JsonObject();
    private String attribute;
    private String name;

    /**
     * Public no-args constructor required for serialization, do not use.
     */
    public ReusableQueryBuilder() {
    }

    /**
     * Creates a new unbound query-builder. Cannot be executed until it is bound
     * by invoking
     *
     * @param attribute the attribute to query on
     */
    public ReusableQueryBuilder(String attribute) {
        this(null, attribute);
    }

    /**
     * @param storage the storage to bind the reusable query to
     * @param attribute the attribute to query on
     */
    public ReusableQueryBuilder(AsyncStorage<Value> storage, String attribute) {
        query.add(new JsonArray());
        this.attribute = attribute;
        this.name = UUID.randomUUID().toString();
        this.storage = storage;
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
        if (current().size() != 0) {
            query.add(new JsonArray());
        }
    }

    private JsonArray current() {
        return query.getJsonArray(query.size() - 1);
    }

    @Override
    public ReusableQueryBuilder<Value> page(int page) {
        options.put(PAGE, page);
        return this;
    }

    @Override
    public ReusableQueryBuilder<Value> pageSize(int pageSize) {
        options.put(PAGE_SIZE, pageSize);
        return this;
    }

    @Override
    public ReusableQueryBuilder<Value> between(Long minimum, Long maximum) {
        add(new JsonObject().put(BETWEEN, new JsonObject()
                .put(MINIMUM, minimum)
                .put(MAXIMUM, maximum)));
        return this;
    }

    private void add(JsonObject clause) {
        current().add(new JsonObject().put(attribute, clause));
    }

    @Override
    public ReusableQueryBuilder<Value> like(String text) {
        add(new JsonObject().put(LIKE, text));
        return this;
    }

    @Override
    public ReusableQueryBuilder<Value> startsWith(String text) {
        add(new JsonObject().put(STARTS_WITH, text));
        return this;
    }

    @Override
    public ReusableQueryBuilder<Value> in(Comparable... list) {
        List<String> comparables = new ArrayList<>();

        for (Comparable comparable : list) {
            comparables.add(comparable.toString());
        }
        add(new JsonObject().put(IN, comparables));
        return this;
    }

    @Override
    public ReusableQueryBuilder<Value> equalTo(Comparable match) {
        add(new JsonObject().put(EQUAL_TO, match));
        return this;
    }

    @Override
    public ReusableQueryBuilder<Value> matches(String regex) {
        add(new JsonObject().put(MATCHES, regex));
        return this;
    }

    @Override
    public ReusableQueryBuilder<Value> orderBy(String orderByAttribute) {
        options.put(ORDER_BY, orderByAttribute);
        return this;
    }

    @Override
    public ReusableQueryBuilder<Value> order(SortOrder order) {
        options.put(ORDER, order);
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
        return options.getString(ORDER_BY);
    }

    @Override
    public void execute(Handler<AsyncResult<List<Value>>> handler) {
        if (storage == null) {
            throw new IllegalArgumentException(ERROR_REUSABLEQUERY_UNBOUND);
        } else {
            //execute(storage, handler);
        }
    }

    @Override
    public EntryWatcher<Value> poll(Consumer<Value> consumer, TimerSource timer) {
        return null;
    }

    public AsyncStorage<Value> storage() {
        if (storage == null) {
            throw new UnsupportedOperationException(ERROR_REUSABLEQUERY_UNBOUND);
        } else {
            return storage;
        }
    }

    private QueryBuilder<Value> parseQuery(AsyncStorage<Value> storage) {
        QueryBuilder<Value> builder = storage.query(getFirstAttribute(query));
        query.stream().map(entry -> (JsonArray) entry).forEach(clause -> {
            builder.or(getFirstAttribute(clause));

            clause.stream().map(entry -> (JsonObject) entry).forEach(statement -> {
                String attribute = getField(statement);
                JsonObject right = statement.getJsonObject(attribute);
                String operator = getField(right);
                Object value = right.getValue(operator);

                builder.and(attribute);
                switch (operator) {
                    case LIKE:
                        builder.like(value.toString());
                        break;
                    case BETWEEN:
                        Long min = ((JsonObject) value).getLong(MINIMUM);
                        Long max = ((JsonObject) value).getLong(MAXIMUM);
                        builder.between(min, max);
                        break;
                    case IN:
                        JsonArray items = right.getJsonArray(IN);
                        Comparable[] list = new Comparable[items.size()];
                        for (int i = 0; i < items.size(); i++) {
                            list[i] = (Comparable) items.getValue(i);
                        }
                        builder.in(list);
                        break;
                    case STARTS_WITH:
                        builder.startsWith(value.toString());
                        break;
                    case EQUAL_TO:
                        builder.equalTo((Comparable) value);
                        break;
                    case MATCHES:
                        builder.equalTo((Comparable) value);
                        break;
                    default:
                        throw new QueryFormatException(getUnknownOperator(operator));
                }
            });
        });
        return builder;
    }

    /**
     * Finds the name of the first attribute within a query, searches through
     * nested arrays if required..
     *
     * @param block may be a query, an array of arrays with statements (JsonObject)
     *              or an array with statements (JsonObject).
     * @return the name of the first attribute within a query.
     */
    private String getFirstAttribute(JsonArray block) {
        Optional<Object> clause = block.stream().findFirst();

        if (clause.isPresent()) {
            Optional<JsonObject> statement = Optional.empty();
            if (clause.get() instanceof JsonArray) {
                statement = ((JsonArray) clause.get()).stream()
                        .map(entry -> (JsonObject) entry)
                        .findFirst();
            } else if (clause.get() instanceof JsonObject) {
                statement = Optional.of((JsonObject) clause.get());
            }
            if (statement.isPresent()) {
                return getField(statement.get());
            }
        }
        throw new QueryFormatException(serialize().encode());
    }

    public JsonObject serialize() {
        return new JsonObject()
                .put(ID_QUERY, query)
                .put(ID_OPTIONS, options);
    }

    private String getField(JsonObject statement) {
        Optional<String> field = statement.fieldNames().stream().findFirst();
        if (field.isPresent()) {
            return field.get();
        } else {
            throw new QueryFormatException(serialize().encode());
        }
    }

    private QueryBuilder<Value> parseSettings(QueryBuilder<Value> builder) {
        for (String option : options.fieldNames()) {
            switch (option) {
                case PAGE_SIZE:
                    builder.pageSize(options.getInteger(option));
                    break;
                case PAGE:
                    builder.page(options.getInteger(option));
                    break;
                case ORDER_BY:
                    builder.orderBy(options.getString(option));
                    break;
                case ORDER:
                    builder.order(SortOrder.valueOf(options.getString(option)));
                    break;
                default:
                    throw new QueryFormatException(getUnknownOption(option));
            }
        }
        return builder;
    }

    @Override
    public String name() {
        return name;
    }
}
