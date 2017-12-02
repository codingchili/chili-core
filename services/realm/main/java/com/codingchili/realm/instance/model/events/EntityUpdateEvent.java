package com.codingchili.realm.instance.model.events;

import com.codingchili.realm.instance.model.Entity;

/**
 * @author Robin Duda
 */
public class EntityUpdateEvent {
    private Entity updated;

    public EntityUpdateEvent(Entity updated) {
        this.updated = updated;
    }

    public Entity getUpdated() {
        return updated;
    }
}
