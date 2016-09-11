package Realm.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Robin Duda
 *         Represents a characters inventory.
 */
class Inventory implements Serializable {
    private HashMap<String, Item> equipped;
    private ArrayList<String> slots;
    private ArrayList<Item> items;
    private Integer space;
    private Integer crystals = 1;

    public Integer getCrystals() {
        return crystals;
    }

    public void setCrystals(Integer crystals) {
        this.crystals = crystals;
    }

    public HashMap<String, Item> getEquipped() {
        return equipped;
    }

    public void setEquipped(HashMap<String, Item> equipped) {
        this.equipped = equipped;
    }

    public Integer getSpace() {
        return space;
    }

    public void setSpace(Integer space) {
        this.space = space;
    }

    public ArrayList<String> getSlots() {
        return slots;
    }

    public void setSlots(ArrayList<String> slots) {
        this.slots = slots;
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }
}
