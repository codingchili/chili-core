package com.codingchili.core.storage;

import io.vertx.core.json.JsonObject;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.files.Configurations;

import static com.codingchili.core.configuration.CoreStrings.STORAGE_ARRAY;
import static com.codingchili.core.protocol.Serializer.getValueByPath;

/**
 * @author Robin Duda
 *         <p>
 *         Base class for the query builder.
 *         <p>
 *         Removes any array notations formatted as {@link CoreStrings#STORAGE_ARRAY}
 *         If an arrayNotation is supplied in the constructor, it will instead be replaced
 *         with the specified notation.
 */
abstract class AbstractQueryBuilder<Value> implements QueryBuilder<Value> {
    boolean isOrdered = false;
    SortOrder sortOrder = SortOrder.ASCENDING;
    int pageSize = Configurations.storage().getMaxResults();
    int page = 0;

    private boolean isAttributeArray = false;
    private String orderByAttribute;
    private String arrayNotation = "";
    private String attribute;

    /**
     * Creates a new query builder with specified attribute. Array notations will be removed.
     *
     * @param attribute the attribute to query for
     */
    AbstractQueryBuilder(String attribute) {
        this(attribute, STORAGE_ARRAY);
    }

    /**
     * Creates a new query builder with the specified attribute, and defines a new
     * array notation.
     *
     * @param attribute     the attribute to query for
     * @param arrayNotation the new array notation
     */
    AbstractQueryBuilder(String attribute, String arrayNotation) {
        this.arrayNotation = arrayNotation;
        setAttribute(attribute);
    }

    @Override
    public QueryBuilder<Value> page(int page) {
        this.page = page;
        return this;
    }

    @Override
    public QueryBuilder<Value> pageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    @Override
    public QueryBuilder<Value> orderBy(String orderByAttribute) {
        this.orderByAttribute = setArrayNotation(orderByAttribute);
        this.isOrdered = true;
        return this;
    }

    private String setArrayNotation(String attribute) {
        return attribute.replace(STORAGE_ARRAY, arrayNotation);
    }

    @Override
    public String attribute() {
        return attribute;
    }

    @Override
    public void setAttribute(String attribute) {
        isAttributeArray = attribute.contains(STORAGE_ARRAY);
        this.attribute = setArrayNotation(attribute);
    }

    @Override
    public boolean isAttributeArray() {
        return isAttributeArray;
    }

    @Override
    public String getOrderByAttribute() {
        if (orderByAttribute == null) {
            return attribute;
        } else {
            return orderByAttribute;
        }
    }

    @Override
    public QueryBuilder<Value> order(SortOrder order) {
        this.sortOrder = order;
        this.isOrdered = true;
        return this;
    }

    int sortByAttribute(JsonObject first, JsonObject second) {
        if (isOrdered) {
            return getSortValue(first).compareTo(getSortValue(second)) * getSortDirection();
        } else
            return 0;
    }

    private String getSortValue(JsonObject object) {
        return getValueByPath(object, getOrderByAttribute())[0].toString();
    }

    int getSortDirection() {
        switch (sortOrder) {
            case ASCENDING:
                return 1;
            case DESCENDING:
                return -1;
            default:
                return 1;
        }
    }
}
