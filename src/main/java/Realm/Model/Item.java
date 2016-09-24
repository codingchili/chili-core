package Realm.Model;

import java.io.Serializable;

/**
 * @author Robin Duda
 *         Contains item data.
 */
class Item implements Serializable {
    private String name;
    private String description;
    private Attributes attributes;

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

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
