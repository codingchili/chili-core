package com.codingchili.core.storage.query;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Robin Duda
 *
 *
 */
public class QueryExpression {
    private Set<QueryConstraint> constraints = new HashSet<>();
    private QueryOperator operator = QueryOperator.NONE;
    private String attribute = "";
    //private ArrayList<QueryMatcher> matchers = new ArrayList<>();
    //private ArrayList<QueryMatcher> matchers = new ArrayList<>();

    //public void addMatcher(QueryMatcher matcher) {
    //    matchers.add(matcher);
   // }

    public void setOperator(QueryOperator operator) {
        this.operator = operator;
    }

    public QueryOperator getOperator() {
        return operator;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getAttribute() {
        return attribute;
    }

    public void addConstraint(QueryConstraint constraint) {
        constraints.add(constraint);
    }
}
