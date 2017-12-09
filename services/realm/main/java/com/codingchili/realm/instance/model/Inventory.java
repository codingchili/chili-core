package com.codingchili.realm.instance.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Robin Duda
 * Represents a characters inventory.
 */
public class Inventory implements Serializable {
    public static Inventory EMPTY = new Inventory();

    private Map<Slot, Item> equipped = new HashMap<>();
    private List<Item> items = new ArrayList<>();
    private Integer space;
    private Integer currency = 1;

    public void equip(int index) {
        if (items.size() < index) {
            Item item = items.get(index);

            if (!item.getSlot().equals(Slot.none)) {
                items.add(equipped.replace(item.getSlot(), item));
                items.remove(item);
            }
        }
    }

    public Integer getCurrency() {
        return currency;
    }

    public void setCurrency(Integer currency) {
        this.currency = currency;
    }

    public Map<Slot, Item> getEquipped() {
        return equipped;
    }

    public void setEquipped(HashMap<Slot, Item> equipped) {
        this.equipped = equipped;
    }

    public Integer getSpace() {
        return space;
    }

    public void setSpace(Integer space) {
        this.space = space;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
}
