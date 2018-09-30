package com.codingchili.core.storage;

import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.context.FutureHelper;
import com.codingchili.core.context.TimerSource;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Consumer;

/**
 * Provides the ability to create a query without an existing storage.
 * <p>
 * Can be used to create reusable queries and string based/serializable queries.
 * <p>
 * If the query is to be executed the #{@link #storage must be set or an exception will be thrown.}
 */
public class Query<Value extends Storable> implements QueryBuilder<Value> {
    private static final String NO_NAME = "unnamed";
    public static final String ON = "ON";
    public static final String AND = "AND";
    public static final String OR = "OR";
    public static final String PAGE = "PAGE";
    public static final String PAGESIZE = "PAGESIZE";
    public static final String BETWEEN = "BETWEEN";
    public static final String LIKE = "LIKE";
    public static final String STARTSWITH = "STARTSWITH";
    public static final String IN = "IN";
    public static final String EQ = "EQ";
    public static final String ORDERBY = "ORDERBY";
    public static final String REGEX = "REGEX";
    public static final String QUERY = "QUERY";
    public static final String NAMED = "NAMED";

    private LinkedList<Runnable> proxy = new LinkedList<>();
    private StringBuilder query = new StringBuilder();
    private StringBuilder options = new StringBuilder();
    private QueryBuilder<Value> builder;
    private AsyncStorage<Value> storage;
    private Consumer<Value> mapper;
    private String name = NO_NAME;

    private void append(String format, Object... params) {
        query.append(String.format(format, params));
        query.append(" ");
    }

    @Override
    public Query<Value> and(String attribute) {
        proxy.add(() -> builder.and(attribute));
        append("%s %s", AND, attribute);
        return this;
    }

    @Override
    public Query<Value> or(String attribute) {
        proxy.add(() -> builder.or(attribute));
        append("\n\t%s %s", OR, attribute);
        return this;
    }

    @Override
    public Query<Value> on(String attribute) {
        proxy.add(() -> builder.on(attribute));
        append("\n\t%s %s", ON, attribute);

        if (NO_NAME.equals(name)) {
            query.insert(0, String.format("%s ", QUERY));
        }

        return this;
    }

    @Override
    public Query<Value> page(int page) {
        proxy.add(() -> builder.page(page));
        option("%s %d", PAGE, page);
        return this;
    }

    @Override
    public Query<Value> pageSize(int pageSize) {
        proxy.add(() -> builder.pageSize(pageSize));
        option("%s %d", PAGESIZE, pageSize);
        return this;
    }

    @Override
    public Query<Value> between(Long minimum, Long maximum) {
        proxy.add(() -> builder.between(minimum, maximum));
        append("%s %d %d", BETWEEN, minimum, maximum);
        return this;
    }

    @Override
    public Query<Value> like(String text) {
        proxy.add(() -> builder.like(text));
        append("%s %s", LIKE, text);
        return this;
    }

    @Override
    public Query<Value> startsWith(String text) {
        proxy.add(() -> builder.startsWith(text));
        append("%s %s", STARTSWITH, text);
        return this;
    }

    @Override
    public Query<Value> in(Comparable... list) {
        proxy.add(() -> builder.in(list));
        StringBuilder in = new StringBuilder();
        in.append("(");
        for (int i = 0; i < list.length; i++) {
            in.append(list[i] + "");
            if (i < list.length - 1) {
                in.append(",");
            }
        }
        in.append(")");
        append("%s %s", IN, in.toString());
        return this;
    }

    @Override
    public Query<Value> equalTo(Comparable match) {
        proxy.add(() -> builder.equalTo(match));
        append("%s %s", EQ, match + "");
        return this;
    }

    @Override
    public Query<Value> matches(String regex) {
        proxy.add(() -> builder.matches(regex));
        append("%s(%s)", REGEX, regex);
        return this;
    }

    @Override
    public Query<Value> orderBy(String orderByAttribute) {
        proxy.add(() -> builder.orderBy(orderByAttribute));
        option("%s %s", ORDERBY, orderByAttribute);
        return this;
    }

    @Override
    public Query<Value> order(SortOrder order) {
        proxy.add(() -> builder.order(order));
        option("%s", order.name());
        return this;
    }

    private void option(String format, Comparable... values) {
        options.append(String.format("%s ", String.format(format, (Object[]) values)));
    }

    /**
     * If intending to invoke either #{@link #execute(Handler)} or #{@link #poll(Consumer, TimerSource)}
     * this method must be called first.
     *
     * @param storage the storage to use if the standalone query is executed.
     * @return fluent.
     */
    public Query<Value> storage(AsyncStorage<Value> storage) {
        this.storage = storage;
        return this;
    }

    @Override
    public void execute(Handler<AsyncResult<Collection<Value>>> handler) {
        assertStorageIsSet();

        // allow calling execute multiple times.
        if (builder == null) {
            builder = storage.query();
            proxy.forEach(Runnable::run);
        }

        builder.execute(execute -> {
            if (execute.succeeded()) {
                Collection<Value> result = execute.result();
                result.forEach(mapper);
                handler.handle(FutureHelper.result(result));
            } else {
                handler.handle(FutureHelper.error(execute.cause()));
            }
        });

    }

    /**
     * Sets a mapping function that can be used to intercept results.
     *
     * @param mapper a mapper that is called for each result when the query is executed.
     * @return fluent.
     */
    public Query<Value> mapper(Consumer<Value> mapper) {
        this.mapper = mapper;
        return this;
    }

    @Override
    public EntryWatcher<Value> poll(Consumer<Collection<Value>> consumer, TimerSource timer) {
        assertStorageIsSet();
        return new EntryWatcher<>(storage, () -> this, timer);
    }

    private void assertStorageIsSet() {
        if (storage == null) {
            throw new CoreRuntimeException("Must call Query::storage before executing a standalone query.");
        }
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Query<Value> setName(String name) {
        this.name = name;
        query.insert(0, String.format("%s %s '%s' ", NAMED, QUERY, name));
        return this;
    }

    @Override
    public String toString() {
        String result = query.append("\n").append(options.toString()).toString();

        // set a name on the query if it has been set.
        if (!name.equals(NO_NAME)) {
            result = result.replace(String.format("%s %s", QUERY, ON),
                    String.format("%s %s '%s' %s\n   ", NAMED, QUERY, ON, name));
        }

        return result;
    }
}
