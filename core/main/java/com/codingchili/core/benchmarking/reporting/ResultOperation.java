package com.codingchili.core.benchmarking.reporting;

import java.util.ArrayList;
import java.util.List;

/**
 * The results of one operation (benchmark) for every implementation in a group.
 */
public class ResultOperation {
    private final List<ResultItem> items = new ArrayList<>();
    private final String name;

    /**
     * @param name the name of the operation.
     */
    public ResultOperation(String name) {
        this.name = name;
    }

    /**
     * @param item the result of an implementation for this operation.
     */
    public void add(ResultItem item) {
        items.add(item);
    }

    /**
     * Compares every implementation with the fastest implementation of this operation.
     *
     * @return fluent
     */
    public ResultOperation compare() {
        int fastest = items.stream().mapToInt(ResultItem::getRate).max().orElse(0);
        items.forEach(item -> item.compareTo(fastest));
        return this;
    }

    /**
     * @return the results in the same implementation order for every operation.
     */
    public List<ResultItem> getItems() {
        return items;
    }

    /**
     * @return the name of the fastest implementation.
     */
    public String getFastest() {
        return items.stream().filter(ResultItem::isFastest)
                .map(ResultItem::getImplementation)
                .findFirst().orElse("");
    }

    public String getName() {
        return name;
    }
}
