package com.codingchili.core.storage;

/**
 * Parses string based queries.
 */
public interface StringQueryParser {
    /**
     * Parses a string into a builder.
     *
     * @param builder    used to construct the query from the given string
     * @param expression the expression to parse.
     * @return the constructed query
     */
    QueryBuilder parse(QueryBuilder<?> builder, String expression);
}
