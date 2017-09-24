package com.codingchili.core.storage;

import com.codingchili.core.context.StorageContext;
import com.codingchili.core.protocol.Serializer;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Robin Duda
 * <p>
 * Query implementations for non-indexed json streams.
 * Use an indexed collection if performance is needed.
 * This implementation is mostly for testing.
 */
class JsonStreamQuery<Value extends Storable> {
    private StreamSource<JsonObject> source;
    private AsyncStorage<Value> storage;
    private StorageContext<Value> context;

    JsonStreamQuery(AsyncStorage<Value> storage, StreamSource<JsonObject> stream) {
        this.storage = storage;
        this.source = stream;
        this.context = storage.context();
    }

    public QueryBuilder<Value> query(String attribute) {
        return new AbstractQueryBuilder<Value>(storage, attribute) {
            List<List<StatementPredicate>> statements = new ArrayList<>();
            int bucketIndex = 0;

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

            @Override
            public void execute(Handler<AsyncResult<Collection<Value>>> handler) {
                context.blocking(task -> {
                    Stream<JsonObject> stream = results().stream();

                    task.complete(stream
                            .sorted(this::sortByAttribute)
                            .skip(page * pageSize)
                            .limit(pageSize)
                            .map(json -> context.toValue(json))
                            .collect(Collectors.toList()));
                }, handler);
            }

            private Set<JsonObject> results() {
                Set<JsonObject> hits = new HashSet<>();

                for (List<StatementPredicate> clause : statements) {
                    Stream<JsonObject> stream = source.stream();

                    for (StatementPredicate statement : clause) {
                        stream = stream.filter(entry -> {
                            return anyMatch(entry, statement);
                        });
                    }
                    hits.addAll(stream.collect(Collectors.toList()));
                }
                return hits;
            }

            private boolean anyMatch(JsonObject entry, StatementPredicate statement) {
                for (Comparable comparable : Serializer.<Comparable>getValueByPath(entry, statement.attribute)) {
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
