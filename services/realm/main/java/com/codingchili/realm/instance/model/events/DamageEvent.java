package com.codingchili.realm.instance.model.events;

import com.codingchili.realm.instance.model.entity.Creature;
import com.codingchili.realm.instance.model.spells.DamageType;

/**
 * @author Robin Duda
 *
 * Event fired when a creature takes damage;
 */
public class DamageEvent implements Event {
    private Creature target;
    private DamageType damage;
    private double value;

    public DamageEvent(Creature target, double value, DamageType damage) {
        this.target = target;
        this.damage = damage;
        this.value = value;
    }

    public Creature getTarget() {
        return target;
    }

    public void setTarget(Creature target) {
        this.target = target;
    }

    public void setDamage(DamageType damage) {
        this.damage = damage;
    }

    public DamageType getDamage() {
        return damage;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public EventType getType() {
        return EventType.DAMAGE;
    }

    @Override
    public Broadcast getBroadcast() {
        return Broadcast.PARTITION;
    }
}
