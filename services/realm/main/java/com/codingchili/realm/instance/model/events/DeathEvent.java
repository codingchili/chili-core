package com.codingchili.realm.instance.model.events;

import com.codingchili.realm.instance.model.Entity;

/**
 * @author Robin Duda
 */
public class DeathEvent implements Event {
    private Entity dead;
    private Entity cause;

    public DeathEvent(Entity dead, Entity cause) {
        this.dead = dead;
        this.cause = cause;
    }

    public Entity getDead() {
        return dead;
    }

    public Entity getCause() {
        return cause;
    }

    @Override
    public EventType getType() {
        return EventType.DEATH;
    }
}
