package com.codingchili.core.storage;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.Collection;

/**
 * @author Robin Duda
 *
 * Interface for the query builder.
 */
public interface QueryBuilder<Value> {
    /**
     * Adds a new AND clause to the query.
     * @param attribute the name of the attribute to be queried.
     * @return fluent
     */
    QueryBuilder and(String attribute);

    /**
     * Adds a new OR clause to the query.
     * @param attribute the name of the attribute to be queried.
     * @return fluent
     */
    QueryBuilder or(String attribute);

    /**
     * set the page offset for paging support.
     * @param page the page to return results from.
     * @return fluent
     */
    QueryBuilder page(int page);

    /**
     * set the size of each page for paging support.
     * @param pageSize the number of hits returned on each page.
     * @return fluent
     */
    QueryBuilder pageSize(int pageSize);

    /**
     *
     * @param minimum
     * @param maximum
     * @return fluent
     */
    QueryBuilder between(int minimum, int maximum);

    /**
     *
     * @param text
     * @return fluent
     */
    QueryBuilder like(String text);

    /**
     *
     * @param text
     * @return fluent
     */
    QueryBuilder startsWith(String text);

    /**
     *
     * @param list
     * @return fluent
     */
    QueryBuilder in(Comparable... list);

    /**
     *
     * @param handler
     */
    void execute(Handler<AsyncResult<Collection<Value>>> handler);
}
