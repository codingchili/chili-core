package com.codingchili.core.Realm.Instance.Model;

import java.io.Serializable;

/**
 * @author Robin Duda
 *         Contains item data.
 */
class Item extends Attributes implements Serializable {
    private String name;
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
