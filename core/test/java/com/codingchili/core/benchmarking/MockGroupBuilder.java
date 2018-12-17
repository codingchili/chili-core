package com.codingchili.core.benchmarking;

import java.util.*;

import com.codingchili.core.context.CoreContext;

/**
 * @author Robin Duda
 * <p>
 * Mock implementation for a benchmark group.
 */
public class MockGroupBuilder extends BenchmarkGroupBuilder {
    private boolean executed = false;
    private MockImplementationBuilder firstImplementation;
    private MockImplementationBuilder secondImplementation;

    /**
     * Creates a new mock group with two mock implementations.
     *
     * @param context    used to execute asynchronous benchmarks.
     * @param name       the handler of the group
     * @param iterations number of iterations to perform
     */
    public MockGroupBuilder(CoreContext context, String name, int iterations) {
        super(name, iterations);
        firstImplementation = new MockImplementationBuilder(context, this, "implementation#1");
        secondImplementation = new MockImplementationBuilder(context, this, "implementation#2");
        add(firstImplementation);
        add(secondImplementation);
    }

    @Override
    public Collection<BenchmarkImplementation> getImplementations() {
        executed = true;
        return super.getImplementations();
    }

    public MockImplementationBuilder getFirstImplementation() {
        return firstImplementation;
    }

    public MockImplementationBuilder getSecondImplementation() {
        return secondImplementation;
    }

    public boolean isExecuted() {
        return executed;
    }
}
