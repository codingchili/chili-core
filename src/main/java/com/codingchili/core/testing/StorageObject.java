package com.codingchili.core.testing;

import io.vertx.core.shareddata.Shareable;

import java.io.Serializable;

/**
 * @author Robin Duda
 */
public class StorageObject implements Shareable, Serializable {
    private String id;
    private Integer level;

    public StorageObject() {}

    public StorageObject(String id, Integer level) {
        this.id = id;
        this.level = level;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String toString() {
        return "id=" + id + " " + "level=" + level;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof StorageObject) && (this.toString().equals(other.toString()));
    }
}
