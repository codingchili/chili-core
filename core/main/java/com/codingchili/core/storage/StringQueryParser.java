package com.codingchili.core.storage;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.Collection;

/**
 * Parses string based queries.
 */
@FunctionalInterface
public interface StringQueryParser<T extends Storable> {
    /**
     * Parses a string into a builder.
     *
     * @param expression the expression to parse.
     * @return the constructed query
     */
    Handler<AsyncResult<Collection<T>>> parse(String expression);
}
