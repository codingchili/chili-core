package com.codingchili.realm.instance.model.events;

import com.codingchili.realm.instance.model.entity.Creature;

/**
 * @author Robin Duda
 *
 * Event emitted when a creature dies.
 */
public class DeathEvent implements Event {
    private Creature target;
    private Creature source;

    public DeathEvent(Creature target, Creature source) {
        this.target = target;
        this.source = source;
    }

    public String getTargetId() {
        return target.getId();
    }

    public String getSourceId() {
        return source.getId();
    }

    public void setDead(Creature dead) {
        this.target = dead;
    }

    public void setCause(Creature cause) {
        this.source = cause;
    }

    @Override
    public EventType getType() {
        return EventType.DEATH;
    }
}
