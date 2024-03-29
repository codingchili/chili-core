package com.codingchili.core.storage;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.TimerSource;
import com.codingchili.core.files.Configurations;

import static com.codingchili.core.configuration.CoreStrings.STORAGE_ARRAY;

/**
 * Base class for the query builder.
 * <p>
 * Removes any array notations formatted as {@link CoreStrings#STORAGE_ARRAY}
 * If an arrayNotation is supplied in the constructor, it will instead be replaced
 * with the specified notation.
 */
public abstract class AbstractQueryBuilder<Value extends Storable> implements QueryBuilder<Value> {
    private boolean isOrdered = false;
    private SortOrder sortOrder = SortOrder.ASCENDING;
    private int pageSize = Configurations.storage().getMaxResults();
    private int page = 0;
    private AsyncStorage<Value> storage;
    private String name = UUID.randomUUID().toString();
    private boolean isAttributeArray = false;
    private String orderByAttribute;
    private String arrayNotation = "";
    private String attribute;

    /**
     * Creates a new query builder with specified attribute. Array notations will be removed.
     *
     * @param storage the backing storage implementation.
     */
    AbstractQueryBuilder(AsyncStorage<Value> storage) {
        this(storage, STORAGE_ARRAY);
    }

    /**
     * Creates a new query builder with the specified attribute, and defines a new
     * array notation.
     *
     * @param arrayNotation the new array notation
     */
    AbstractQueryBuilder(AsyncStorage<Value> storage, String arrayNotation) {
        this.arrayNotation = arrayNotation;
        this.storage = storage;
    }

    @Override
    public EntryWatcher<Value> poll(Consumer<Collection<Value>> consumer, TimerSource timer) {
        return new EntryWatcher<>(storage, () -> this, timer).start(consumer);
    }

    @Override
    public QueryBuilder<Value> on(String attribute) {
        setAttribute(attribute);
        return this;
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

    public String attribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        if (attribute != null) {
            isAttributeArray = attribute.contains(STORAGE_ARRAY);
            this.attribute = setArrayNotation(attribute);
        }
    }

    public boolean isAttributeArray() {
        return isAttributeArray;
    }

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

    @Override
    public String name() {
        return name;
    }

    @Override
    public QueryBuilder<Value> setName(String name) {
        this.name = name;
        return this;
    }

    int sortByAttribute(Object first, Object second) {
        if (isOrdered) {
            return getSortValue(first).compareTo(getSortValue(second)) * getSortDirection();
        } else
            return 0;
    }

    private String getSortValue(Object object) {
        return AttributeRegistry.get(object.getClass(), getOrderByAttribute())
                .getValues(object, null)
                .iterator().next().toString();
    }

    int getSortDirection() {
        return switch (sortOrder) {
            case ASCENDING -> 1;
            case DESCENDING -> -1;
        };
    }

    public boolean isOrdered() {
        return isOrdered;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getPage() {
        return page;
    }

    public String getName() {
        return name;
    }

    public String getArrayNotation() {
        return arrayNotation;
    }

    public String getAttribute() {
        return attribute;
    }
}
