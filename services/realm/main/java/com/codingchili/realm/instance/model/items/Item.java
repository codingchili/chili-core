package com.codingchili.realm.instance.model.items;

import com.codingchili.realm.instance.model.stats.Modifiers;
import com.codingchili.realm.instance.model.stats.Stats;

/**
 * @author Robin Duda
 */
public class Item {
    protected String name = "no name";
    protected String description = "no description.";
    protected Slot slot = Slot.none;
    protected Boolean usable = null;
    protected Stats stats = new Stats();
    protected Modifiers modifiers = new Modifiers();
    protected String onHit = null; // jexl script or reference to affliction?
    protected String onDamaged = null; // jexl script or reference to affliction?
    protected Recipe recipe = new Recipe();

    public Boolean isUsable() {
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

    public Stats getStats() {
        return stats;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }

    public Modifiers getModifiers() {
        return modifiers;
    }

    public void setModifiers(Modifiers modifiers) {
        this.modifiers = modifiers;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public String getOnHit() {
        return onHit;
    }

    public void setOnHit(String onHit) {
        this.onHit = onHit;
    }

    public String getOnDamaged() {
        return onDamaged;
    }

    public void setOnDamaged(String onDamaged) {
        this.onDamaged = onDamaged;
    }
}
