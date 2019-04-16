package com.codingchili.core.storage;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.Collection;
import java.util.function.Consumer;

import com.codingchili.core.context.TimerSource;

/**
 * Interface for the query builder.
 * <p>
 * All queries must start with a single attribute name, this is usually passed
 * onto the query-builder constructor. To add constraints to the attribute
 * use [between, like, equal, in.. etc], the attribute constraints are
 * applied using the AND operator.
 * <p>
 * To match documents that matches at least one constraint for a single attribute,
 * use the OR operation with the same attribute name and then apply the constraint.
 * <p>
 * To create more complex queries that spans over multiple attributes, use the AND/OR
 * operation to combine/group constraints for attributes. Following are some examples of
 * usage:
 * <p>
 * Matches all documents where..
 * <p>
 * attribute name starts with "veg" or contains "table'
 * - attribute "name" startsWith "veg" or attribute "name" like "table"
 * <p>
 * attribute name starts with "veg" AND contains "table'.
 * - attribute "name" startsWith "veg" like "table"
 * <p>
 * attribute name equals "vegetable" or "red".
 * - attribute "name" equals "vegetable" or attribute "name" equals "red"
 * <p>
 * attribute name contains "veg" or "fruit".
 * - attribute "name" like "veg" or attribute "name" like "fruit"
 * <p>
 * attribute vegetable.color, json example: {vegetable: {color: ""}} is red, green or blue.
 * - attribute "vegetable.color" in [red, green, blue]
 * <p>
 * attribute vegetable.color is "red", all matches are ordered by vegetable.price
 * - attribute "vegetable.color" equals "red' orderBy "vegetable.price" descending
 * <p>
 * attribute vegetable.vitamins is [A,B,C], match if vegetable contains vitamin C or K
 * - attribute "vegetable.vitamins[]" in [C,K]
 */
public interface QueryBuilder<Value extends Storable> {
    /**
     * Adds a new AND clause to the query.
     *
     * @param attribute the name of the attribute to be queried.
     * @return fluent
     */
    QueryBuilder<Value> and(String attribute);

    /**
     * Adds a new OR clause to the query.
     *
     * @param attribute the name of the attribute to be queried.
     * @return fluent
     */
    QueryBuilder<Value> or(String attribute);

    /**
     * The attribute to be queried.
     * @param attribute the name of the queried attribute.
     * @return fluent
     */
    QueryBuilder<Value> on(String attribute);

    /**
     * set the page offset for paging support.
     *
     * @param page the page to return results from.
     * @return fluent
     */
    QueryBuilder<Value> page(int page);

    /**
     * set the size of each page for paging support.
     *
     * @param pageSize the number of hits returned on each page.
     * @return fluent
     */
    QueryBuilder<Value> pageSize(int pageSize);

    /**
     * Matches documents with the attribute between and including minimum and maximum.
     *
     * @param minimum the minimum value to match
     * @param maximum the maximum value to match
     * @return fluent
     */
    QueryBuilder<Value> between(Long minimum, Long maximum);

    /**
     * Matches documents where the attribute containst the given text
     *
     * @param text the text to check if contained.
     * @return fluent
     */
    QueryBuilder<Value> like(String text);

    /**
     * Matches if the specified attribute starts with the given text
     *
     * @param text the text to check if starting with.
     * @return fluent
     */
    QueryBuilder<Value> startsWith(String text);

    /**
     * Matches if a specified attribute is contained within the given list
     *
     * @param list the list that the attribute value must be contained in to match.
     * @return fluent
     */
    QueryBuilder<Value> in(Comparable... list);

    /**
     * Matches if the attribute is an exact match to the given text.
     *
     * @param match the text that should be equal to the specified attribute.
     * @return fluent
     */
    QueryBuilder<Value> equalTo(Comparable match);

    /**
     * Checks if the given attribute matches the given regex. Be careful when passing
     * user input to this method. No complexity restrictions are applied. To pass user
     * input use equals instead. If a query can use any non-regex constraint, it is
     * recommended to use that instead.
     *
     * @param regex the regular expression in which the attribute is to match
     * @return fluent
     */
    QueryBuilder<Value> matches(String regex);

    /**
     * Orders the result by the given attribute using the default sort order unless
     * the sort order has been explicitly set.
     *
     * @param orderByAttribute the handler of the attribute to sort by using dot notation.
     * @return fluent
     */
    QueryBuilder<Value> orderBy(String orderByAttribute);

    /**
     * Orders the result by the given direction using the last specified attribute
     * handler unless orderBy is set.
     *
     * @param order descending or ascending.
     * @return fluent
     */
    QueryBuilder<Value> order(SortOrder order);

    /**
     * Executes the constructed query asynchronously.
     *
     * @param handler the handler to be invoked when the result is completed.
     */
    void execute(Handler<AsyncResult<Collection<Value>>> handler);

    /**
     * Executes the query periodically.
     *
     * @param consumer a consumer that receives the results
     * @param timer    the source of the interval.
     * @return fluent.
     */
    EntryWatcher<Value> poll(Consumer<Collection<Value>> consumer, TimerSource timer);

    /**
     * Generates unique ids for the triggers that are used. Should be overridden
     * so that logging messages are a bit meaningful.
     *
     * @return a name that identifies this query.
     */
    String name();


    /**
     * Sets a name for the query for identification.
     *
     * @param name the name to set
     * @return fluent
     */
    QueryBuilder<Value> setName(String name);
}
