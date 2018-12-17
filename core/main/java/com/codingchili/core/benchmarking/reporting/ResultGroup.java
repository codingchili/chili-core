package com.codingchili.core.benchmarking.reporting;

import java.util.ArrayList;
import java.util.List;

/**
 * Root container for all result objects.
 */
public class ResultGroup {
    private List<ResultSet> sets = new ArrayList<>();
    private String name;
    private int iterations;

    public ResultGroup(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<ResultSet> getSets() {
        return sets;
    }

    public void add(ResultSet set) {
        this.sets.add(set);
    }

    public int getIterations() {
        return iterations;
    }

    public ResultGroup setIterations(int iterations) {
        this.iterations = iterations;
        return this;
    }
}


