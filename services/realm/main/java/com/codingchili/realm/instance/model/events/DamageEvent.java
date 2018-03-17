package com.codingchili.realm.instance.model.events;

import com.codingchili.realm.instance.model.entity.Creature;
import com.codingchili.realm.instance.model.entity.Entity;
import com.codingchili.realm.instance.model.spells.DamageType;

/**
 * @author Robin Duda
 * <p>
 * Event fired when a creature takes damage;
 */
public class DamageEvent implements Event {
    private Entity target;
    private Entity source;
    private DamageType damage;
    private double value;

    public DamageEvent(Creature target, double value, DamageType damage) {
        this.target = target;
        this.damage = damage;
        this.value = value;
    }

    public DamageEvent setSource(Creature source) {
        this.source = source;
        return this;
    }

    public String getSourceId() {
        if (source != null) {
            return source.getId();
        } else {
            return null;
        }
    }

    public String getTargetId() {
        return target.getId();
    }

    public DamageType getDamage() {
        return damage;
    }

    public double getValue() {
        return value;
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
