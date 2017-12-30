package com.codingchili.realm.instance.model.events;

import com.codingchili.realm.instance.model.entity.Creature;

/**
 * @author Robin Duda
 */
public class EntityUpdateEvent implements Event {
    private Creature updated;

    public EntityUpdateEvent(Creature updated) {
        this.updated = updated;
    }

    public Creature getUpdated() {
        return updated;
    }

    @Override
    public EventType getType() {
        return EventType.UPDATE;
    }
}
