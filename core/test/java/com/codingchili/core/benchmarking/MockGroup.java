package com.codingchili.core.benchmarking;

/**
 * @author Robin Duda
 *
 * Mock implementation for a benchmark group.
 */
public class MockGroup extends BaseBenchmarkGroup {
    private MockImplementation firstImplementation = new MockImplementation(this, "group#1");
    private MockImplementation secondImplementation = new MockImplementation(this, "group#2");

    public MockGroup(String name, int iterations) {
        super(name, iterations);
        add(firstImplementation);
        add(secondImplementation);
    }

    public MockImplementation getFirstImplementation() {
        return firstImplementation;
    }

    public MockImplementation getSecondImplementation() {
        return secondImplementation;
    }
}
