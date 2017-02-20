package com.codingchili.core.storage.query;

import java.util.ArrayList;

import static com.codingchili.core.storage.query.QueryConstraint.Bound.*;

/**
 * @author Robin Duda
 *         <p>
 *         Builds queries for the storage implementations.
 */
public class QueryBuilder {
    private ArrayList<QueryExpression> expressions = new ArrayList<>();
    private QueryExpression expression = new QueryExpression();
    private int maxHits = 1;

    private QueryBuilder next() {
        expressions.add(expression);
        expression = new QueryExpression();
        return this;
    }

    public QueryBuilder or() {
        expression.setOperator(QueryOperator.OR);
        return next();
    }

    public QueryBuilder and() {
        expression.setOperator(QueryOperator.AND);
        return next();
    }

    public QueryBuilder attribute(String name) {
        expression.setAttribute(name);
        return this;
    }

    public QueryBuilder startsWith(String text) {
        expression.addMatcher(new Matcher(Matcher.StartsWith));
        return this;
    }

    public QueryBuilder endsWith(String text) {
       // expression.setMatcher(S)
        return this;
    }

    public QueryBuilder contains(String text) {

        return this;
    }

    public QueryBuilder equals(String text) {

        return this;
    }

    public QueryBuilder greaterThan(int value) {
        return addConstraint(value, GREATER);
    }

    public QueryBuilder lesserThan(int value) {
        return addConstraint(value, LESSER);
    }

    public QueryBuilder equalTo(int value) {
        return addConstraint(value, EQUAL);
    }

    public QueryBuilder unequalTo(int value) {
        return addConstraint(value, NOT_EQUAL);
    }

    private QueryBuilder addConstraint(int value, QueryConstraint.Bound constraint) {
        expression.addConstraint(new QueryConstraint(value, constraint));
        return this;
    }

    public QueryBuilder limit(int maxHits) {
        this.maxHits = maxHits;
        return this;
    }

    public ArrayList<QueryExpression> compile() {
        return expressions;
    }

    class Test {

        public Test() {
            QueryBuilder builder = new QueryBuilder();

            builder.limit(10)
                    .attribute("name")
                    .startsWith("account")
                    .endsWith("_2")
                    .and()
                    .attribute("level")
                    .greaterThan(50)
                    .lesserThan(100);

        }

    }
}
