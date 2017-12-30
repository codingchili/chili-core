package com.codingchili.realm.instance.model.events;

import com.codingchili.realm.instance.model.entity.Creature;

/**
 * @author Robin Duda
 */
public class DeathEvent implements Event {
    private Creature dead;
    private Creature cause;

    public DeathEvent(Creature dead, Creature cause) {
        this.dead = dead;
        this.cause = cause;
    }

    public Creature getDead() {
        return dead;
    }

    public Creature getCause() {
        return cause;
    }

    @Override
    public EventType getType() {
        return EventType.DEATH;
    }
}
