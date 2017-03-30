package com.codingchili.core.benchmarking;

import com.codingchili.core.context.CoreContext;

/**
 * @author Robin Duda
 *         <p>
 *         Mock implementation for a benchmark group.
 */
public class MockGroup extends BaseBenchmarkGroup {

    /**
     * Creates a new mock group with two mock implementations.
     *
     * @param context    used to execute asynchronous benchmarks.
     * @param name       the name of the group
     * @param iterations number of iterations to perform
     */
    public MockGroup(CoreContext context, String name, int iterations) {
        super(name, iterations);
        MockImplementation firstImplementation = new MockImplementation(context, this, "implementation#1");
        MockImplementation secondImplementation = new MockImplementation(context, this, "implementation#2");
        add(firstImplementation);
        add(secondImplementation);

    }
}
