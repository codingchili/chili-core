package com.codingchili.core.storage;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.protocol.Serializer;

/**
 * Query implementations for non-indexed json streams.
 * Use an indexed collection if performance is needed.
 * This implementation is mostly for testing.
 * <p>
 * Value = the type of the result to be returned.
 * Streaming = the type of objects that are queried.
 * <p>
 * If the types of Value and Streaming are disjoint,
 * a mapper must be supplied to map the Streaming values to the Value type.
 */
public class StreamQuery<Value extends Storable, Streaming> {
    private StreamSource<Streaming> source;
    private AsyncStorage<Value> storage;
    private StorageContext<Value> context;

    // if no mapper is set, the streaming source must be the same as the value.
    @SuppressWarnings("unchecked")
    private Function<Streaming, Value> mapper = (v) -> (Value) v;

    StreamQuery(AsyncStorage<Value> storage, StreamSource<Streaming> stream) {
        this.storage = storage;
        this.source = stream;
        this.context = storage.context();
    }

    public StreamQuery<Value, Streaming> setMapper(Function<Streaming, Value> mapper) {
        this.mapper = mapper;
        return this;
    }

    public QueryBuilder<Value> query() {
        return new AbstractQueryBuilder<Value>(storage) {
            List<List<StatementPredicate>> statements = new ArrayList<>();
            int bucketIndex = 0;

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
                bucketIndex++;
                return this;
            }

            private void apply(Predicate<Comparable> predicate) {
                if (statements.size() == bucketIndex) {
                    statements.add(new ArrayList<>());
                }
                currentStatement().add(new StatementPredicate(attribute(), predicate));
            }

            private List<StatementPredicate> currentStatement() {
                if (statements.size() == 0) {
                    statements.add(new ArrayList<>());
                }
                return statements.get(statements.size() - 1);
            }

            @Override
            public QueryBuilder<Value> between(Long minimum, Long maximum) {
                apply(value -> {
                    if (value instanceof Integer) {
                        return ((Integer) value >= minimum.intValue() && (Integer) value <= maximum.intValue());
                    } else
                        return value instanceof Long && (Long) value >= minimum && (Long) value <= maximum;
                });
                return this;
            }

            @Override
            public QueryBuilder<Value> like(String text) {
                apply(entry -> (entry.toString().toLowerCase().contains(text.toLowerCase())));
                return this;
            }

            @Override
            public QueryBuilder<Value> startsWith(String text) {
                apply(entry -> (entry.toString().startsWith(text)));
                return this;
            }

            @Override
            public QueryBuilder<Value> in(Comparable[] list) {
                apply(entry -> {
                    for (Comparable comparable : list) {
                        if (entry.equals(comparable))
                            return true;
                    }
                    return false;
                });
                return this;
            }

            @Override
            public QueryBuilder<Value> equalTo(Comparable match) {
                apply(match::equals);
                return this;
            }

            @Override
            public QueryBuilder<Value> matches(String regex) {
                apply(entry -> entry.toString().matches(regex));
                return this;
            }

            @SuppressWarnings("unchecked")
            @Override
            public void execute(Handler<AsyncResult<Collection<Value>>> handler) {
                context.blocking(task -> {

                    task.complete(results().stream()
                            .sorted(this::sortByAttribute)
                            .skip(getPage() * getPageSize())
                            .limit(getPageSize())
                            .map(mapper)
                            .collect(Collectors.toList()));

                }, handler);
            }

            private Set<Streaming> results() {
                return source.stream().filter(entry -> {

                    if (statements.size() > 0) {
                        // if an entry matches any of the classes it is a hit
                        for (List<StatementPredicate> clause : statements) {
                            boolean match = true;
                            // check if the entry matches all statements in the clause
                            for (StatementPredicate statement : clause) {
                                if (!anyMatch(entry, statement)) {
                                    match = false;
                                }
                            }
                            if (match) {
                                return true;
                            }
                        }
                        return false;
                    } else {
                        // if there are no constraints - consider everything as matching.
                        return true;
                    }
                }).collect(Collectors.toSet());
            }

            // match function that tests all elements in an array if statement.attribute points to one.
            private boolean anyMatch(Streaming entry, StatementPredicate statement) {
                var values = AttributeRegistry.get(entry.getClass(), statement.attribute)
                        .getValues(entry, null);

                for (String comparable : values) {
                    if (statement.predicate.test(comparable))
                        return true;
                }
                return false;
            }
        };
    }

    private class StatementPredicate {
        private final String attribute;
        private final Predicate<Comparable> predicate;

        StatementPredicate(String attribute, Predicate<Comparable> predicate) {
            this.attribute = attribute;
            this.predicate = predicate;
        }
    }
}
