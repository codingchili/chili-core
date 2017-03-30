package com.codingchili.core.benchmarking;

import java.util.List;

import com.codingchili.core.context.CoreContext;

/**
 * @author Robin Duda
 *         <p>
 *         Mock implementation for a benchmark group.
 */
public class MockGroup extends BaseBenchmarkGroup {
    private boolean executed = false;
    private MockImplementation firstImplementation;
    private MockImplementation secondImplementation;

    /**
     * Creates a new mock group with two mock implementations.
     *
     * @param context    used to execute asynchronous benchmarks.
     * @param name       the name of the group
     * @param iterations number of iterations to perform
     */
    public MockGroup(CoreContext context, String name, int iterations) {
        super(name, iterations);
        firstImplementation = new MockImplementation(context, this, "implementation#1");
        secondImplementation = new MockImplementation(context, this, "implementation#2");
        add(firstImplementation);
        add(secondImplementation);
    }

    @Override
    public List<BenchmarkImplementation> getImplementations() {
        executed = true;
        return super.getImplementations();
    }

    public MockImplementation getFirstImplementation() {
        return firstImplementation;
    }

    public MockImplementation getSecondImplementation() {
        return secondImplementation;
    }

    public boolean isExecuted() {
        return executed;
    }
}
