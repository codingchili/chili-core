package com.codingchili.realm.instance.model.entity;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.events.Event;
import com.codingchili.realm.instance.model.items.Inventory;
import com.codingchili.realm.instance.model.afflictions.AfflictionState;
import com.codingchili.realm.instance.model.spells.SpellState;
import com.codingchili.realm.instance.model.stats.Attribute;
import com.codingchili.realm.instance.model.stats.Stats;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Set;
import java.util.UUID;

/**
 * @author Robin Duda
 */
public abstract class SimpleEntity implements Entity {
    private Stats calculated = new Stats();
    protected String id = UUID.randomUUID().toString();
    protected Inventory inventory = Inventory.EMPTY;
    protected AfflictionState afflictions = new AfflictionState();
    protected SpellState spells = new SpellState();
    protected Stats stats = new Stats().add(Attribute.strength, 3).add(Attribute.health, 300);
    protected EventProtocol protocol;
    protected GameContext context;

    protected Vector vector = new Vector()
            .setX((float) (Math.random() * 1000))
            .setY((float) (Math.random() * 1000));

    public SimpleEntity(GameContext context) {
        this.context = context;
        this.protocol = context.subscribe(this);
    }

    @Override
    public void handle(Event event) {
        protocol.get(event.getType().toString()).submit(event);
    }

    @Override
    public Set<String> getInteractions() {
        return protocol.available();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public Vector getVector() {
        return vector;
    }

    @Override
    public AfflictionState getAfflictions() {
        return afflictions;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Stats getBaseStats() {
        return stats;
    }

    public void setBaseStats(Stats stats) {
        this.stats = stats;
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

    @Override
    public String getName() {
        return "<no name>";
    }

    public void setId(String id) {
        this.id = id;
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

    public void setVector(Vector vector) {
        this.vector = vector;
    }
}
