package com.codingchili.services.realm.instance.model;

import java.io.Serializable;

/**
 * @author Robin Duda
 *         Holds the cost of a spell.
 */
class Cost implements Serializable {
    private String requires;
    private Integer value;

    public String getRequires() {
        return requires;
    }

    public void setRequires(String requires) {
        this.requires = requires;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
