package com.codingchili.core.benchmarking.reporting;

import java.util.ArrayList;
import java.util.List;

/**
 * A group contains multiple resultsets, one for each operation.
 */
public class ResultSet {
    private List<ResultItem> items = new ArrayList<>();
    private String name;

    public ResultSet(String name) {
        this.name = name;
    }

    public void add(ResultItem item) {
        this.items.add(item);
    }

    public List<ResultItem> getItems() {
        return items;
    }

    public String getName() {
        return name;
    }
}
