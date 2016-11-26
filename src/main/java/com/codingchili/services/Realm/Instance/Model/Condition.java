package com.codingchili.services.realm.instance.model;

import java.io.Serializable;

/**
 * @author Robin Duda
 *         Whenever a modifier is applied a condition may be specified.
 *         The condition specifies whether the modifier should be applied.
 */
class Condition implements Serializable{
    private String attribute;
    private As as;
    private Matches matches;
    private double value;

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public As getAs() {
        return as;
    }

    public void setAs(As as) {
        this.as = as;
    }

    public Matches getMatches() {
        return matches;
    }

    public void setMatches(Matches matches) {
        this.matches = matches;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
