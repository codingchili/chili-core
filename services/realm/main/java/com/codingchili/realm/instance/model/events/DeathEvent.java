package com.codingchili.realm.instance.model.events;

import com.codingchili.realm.instance.model.entity.Creature;

/**
 * @author Robin Duda
 *
 * Event emitted when a creature dies.
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

    public void setDead(Creature dead) {
        this.dead = dead;
    }

    public void setCause(Creature cause) {
        this.cause = cause;
    }

    @Override
    public EventType getType() {
        return EventType.DEATH;
    }
}
