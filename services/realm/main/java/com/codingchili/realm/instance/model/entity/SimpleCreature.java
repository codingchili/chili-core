package com.codingchili.realm.instance.model.entity;

import com.codingchili.realm.instance.model.afflictions.AfflictionState;
import com.codingchili.realm.instance.model.items.Inventory;
import com.codingchili.realm.instance.model.spells.SpellState;
import com.codingchili.realm.instance.model.stats.Stats;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Robin Duda
 */
public abstract class SimpleCreature extends SimpleEntity implements Creature {
    private Stats calculated = new Stats();
    protected Inventory inventory = new Inventory();
    protected AfflictionState afflictions = new AfflictionState();
    protected SpellState spells = new SpellState();
    protected Stats stats = new Stats();

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public AfflictionState getAfflictions() {
        return afflictions;
    }

    @Override
    public Stats getBaseStats() {
        return stats;
    }

    @Override
    public SpellState getSpells() {
        return spells;
    }

    @JsonIgnore
    public Stats getStats() {
        calculated.clear();
        return calculated.apply(inventory.getStats()).apply(afflictions.getStats()).apply(stats);
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public void setAfflictions(AfflictionState afflictions) {
        this.afflictions = afflictions;
    }

    public void setSpells(SpellState spells) {
        this.spells = spells;
    }

    public void setBaseStats(Stats stats) {
        this.stats = stats;
    }
}
