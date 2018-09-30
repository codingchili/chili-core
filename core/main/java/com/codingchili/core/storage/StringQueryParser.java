package com.codingchili.core.storage;

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
    QueryBuilder<T> parse(String expression);
}
