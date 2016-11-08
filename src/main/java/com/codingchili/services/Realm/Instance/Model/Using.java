package com.codingchili.services.Realm.Instance.Model;

import java.io.Serializable;

/**
 * @author Robin Duda
 *         Allows modification of one players stats by the stats of another players.
 *         Use case: dealing damage, healing, applying afflictions
 */
public class Using implements Serializable {
    private Target source = Target.caster;
    private String attribute;
    private Double value;
    private Boolean cancel;
    private Condition condition;

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public Boolean getCancel() {
        return cancel;
    }

    public void setCancel(Boolean cancel) {
        this.cancel = cancel;
    }

    public Target getSource() {
        return source;
    }

    public void setSource(Target source) {
        this.source = source;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
