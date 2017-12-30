package com.codingchili.realm.instance.model.items;

import com.codingchili.realm.instance.scripting.Scripted;
import com.codingchili.realm.instance.model.stats.Stats;

/**
 * @author Robin Duda
 */
public class Item {
    protected String name = "no name";
    protected String description = "no description.";
    protected Slot slot = Slot.none;
    protected WeaponType weapon = WeaponType.none;
    protected ArmorType armor = ArmorType.none;
    protected Stats stats = new Stats();

    // todo: convert to references to hit effects/ item use scripts.
    // this will be better for serialization, storage and balance changes.
    protected Scripted onHit = null; // jexl script or reference to affliction?
    protected Scripted onDamaged = null; // jexl script or reference to affliction?
    protected Scripted onUse = null; // jexl script consume item etc.


    protected Recipe recipe = new Recipe();

    public Boolean isUsable() {
        return (onUse != null);
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

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public Scripted getOnHit() {
        return onHit;
    }

    public void setOnHit(Scripted onHit) {
        this.onHit = onHit;
    }

    public Scripted getOnDamaged() {
        return onDamaged;
    }

    public void setOnDamaged(Scripted onDamaged) {
        this.onDamaged = onDamaged;
    }

    public WeaponType getWeapon() {
        return weapon;
    }

    public void setWeapon(WeaponType weapon) {
        this.weapon = weapon;
    }

    public ArmorType getArmor() {
        return armor;
    }

    public void setArmor(ArmorType armor) {
        this.armor = armor;
    }
}
