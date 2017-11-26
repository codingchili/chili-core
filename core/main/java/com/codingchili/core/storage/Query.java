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
    private LinkedList<Runnable> proxy = new LinkedList<>();
    private StringBuilder query = new StringBuilder();
    private QueryBuilder<Value> builder;
    private AsyncStorage<Value> storage;
    private Consumer<Value> mapper;
    private String name = "UnnamedStandaloneQuery";
    private String initial;

    private Query(String attribute) {
        this.initial = attribute;
        append("%s %s", "QUERY", attribute);
    }

    private void append(String format, Object... params) {
        query.append(String.format(format, params));
        query.append(" ");
    }

    public static <Value extends Storable> Query<Value> on(String attribute) {
        return new Query<>(attribute);
    }

    @Override
    public Query<Value> and(String attribute) {
        proxy.add(() -> builder.and(attribute));
        append("\n\t%s %s", "AND", attribute);
        return this;
    }

    @Override
    public Query<Value> or(String attribute) {
        proxy.add(() -> builder.or(attribute));
        append("\n\t%s %s", "OR", attribute);
        return this;
    }

    @Override
    public Query<Value> page(int page) {
        proxy.add(() -> builder.page(page));
        append("\n  %s %d", "PAGE", page);
        return this;
    }

    @Override
    public Query<Value> pageSize(int pageSize) {
        proxy.add(() -> builder.pageSize(pageSize));
        append("\n  %s %d", "PAGESIZE", pageSize);
        return this;
    }

    @Override
    public Query<Value> between(Long minimum, Long maximum) {
        proxy.add(() -> builder.between(minimum, maximum));
        append("%s %d %d", "BETWEEN", minimum, maximum);
        return this;
    }

    @Override
    public Query<Value> like(String text) {
        proxy.add(() -> builder.like(text));
        append("%s %s", "LIKE", text);
        return this;
    }

    @Override
    public Query<Value> startsWith(String text) {
        proxy.add(() -> builder.startsWith(text));
        append("%s %s", "STARTSWITH", text);
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
        append("%s %s", "IN", in.toString());
        return this;
    }

    @Override
    public Query<Value> equalTo(Comparable match) {
        proxy.add(() -> builder.equalTo(match));
        append("%s %s", "EQ", match + "");
        return this;
    }

    @Override
    public Query<Value> matches(String regex) {
        proxy.add(() -> builder.matches(regex));
        append("REGEX(%s)", regex);
        return this;
    }

    @Override
    public Query<Value> orderBy(String orderByAttribute) {
        proxy.add(() -> builder.orderBy(orderByAttribute));
        append("\n  %s %s", "ORDERBY", orderByAttribute);
        return this;
    }

    @Override
    public Query<Value> order(SortOrder order) {
        proxy.add(() -> builder.order(order));
        append("\n  %s", order.name());
        return this;
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
            builder = storage.query(initial);
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
        return this;
    }

    @Override
    public String toString() {
        return query.toString();
    }
}
