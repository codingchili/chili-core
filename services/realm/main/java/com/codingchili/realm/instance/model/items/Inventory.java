package com.codingchili.realm.instance.model.items;

import com.codingchili.realm.instance.model.stats.Stats;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Robin Duda
 * <p>
 * Represents a characters inventory.
 */
public class Inventory implements Serializable {
    public static Inventory EMPTY = new Inventory();

    private Map<Slot, Item> equipped = new ConcurrentHashMap<>();
    private List<Item> items = new ArrayList<>();
    private Stats stats = new Stats();
    private Integer space;
    private Integer currency = 1;

    {
        items.add(new WoodenSword());
        equip(0);
    }

    public void equip(int index) {

        // todo: must check if allowed to equip.

        if (index <= items.size()) {
            Item item = items.get(index);

            if (!item.getSlot().equals(Slot.none)) {
                if (equipped.containsKey(item.getSlot())) {
                    items.add(equipped.replace(item.getSlot(), item));
                } else {
                    equipped.put(item.slot, item);
                }
                items.remove(item);
            }
        }
        stats.clear();
        equipped.forEach((slot, item) -> stats = stats.apply(item.getStats()));
    }

    public void add(Item item) {
        items.add(item);
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

    @JsonIgnore
    public Stats getStats() {
        return stats;
    }
}
