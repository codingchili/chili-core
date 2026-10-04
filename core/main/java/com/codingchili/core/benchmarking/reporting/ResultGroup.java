package com.codingchili.core.benchmarking.reporting;

import java.util.*;

/**
 * The results of a benchmark group, organized by operation and by implementation.
 */
public class ResultGroup {
    private final List<ResultOperation> operations = new ArrayList<>();
    private final List<ResultSet> sets = new ArrayList<>();
    private final String name;
    private int iterations;

    /**
     * @param name the name of the benchmark group.
     */
    public ResultGroup(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * @return results per implementation, in the order the implementations were added.
     */
    public List<ResultSet> getSets() {
        return sets;
    }

    /**
     * @return the names of the implementations, in the order they were added.
     */
    public List<String> getImplementations() {
        List<String> names = new ArrayList<>();
        sets.forEach(set -> names.add(set.getName()));
        return names;
    }

    /**
     * @return results per operation, every operation lists the implementations in the same order.
     */
    public List<ResultOperation> getOperations() {
        return operations;
    }

    /**
     * Adds the results of an implementation and indexes them by operation.
     *
     * @param set the results of one implementation.
     */
    public void add(ResultSet set) {
        sets.add(set);
        set.getItems().forEach(item -> operation(item.getName()).add(item));
        operations.forEach(ResultOperation::compare);
    }

    private ResultOperation operation(String name) {
        return operations.stream()
                .filter(operation -> operation.getName().equals(name))
                .findFirst()
                .orElseGet(() -> {
                    ResultOperation operation = new ResultOperation(name);
                    operations.add(operation);
                    return operation;
                });
    }

    public int getIterations() {
        return iterations;
    }

    public ResultGroup setIterations(int iterations) {
        this.iterations = iterations;
        return this;
    }
}
