package com.codingchili.realm.instance.model;

/**
 * @author Robin Duda
 */
public class Item {
    protected Slot slot = Slot.none;
    protected boolean usable = false;
    protected String description;
    protected String name;

    public boolean isUsable() {
        return usable;
    }

    public void setUsable(boolean usable) {
        this.usable = usable;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Slot getSlot() {
        return slot;
    }

    public void setSlot(Slot slot) {
        this.slot = slot;
    }
}
