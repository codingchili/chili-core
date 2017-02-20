package com.codingchili.core.storage.query;

/**
 * @author Robin Duda
 */
public class QueryConstraint {
    private Bound bound;
    private int value;

    public QueryConstraint(int value, Bound bound) {
        this.value = value;
        this.bound = bound;
    }

    public Bound getBound() {
        return bound;
    }

    public int getValue() {
        return value;
    }

    public enum Bound {LESSER, GREATER, EQUAL, NOT_EQUAL}
}
