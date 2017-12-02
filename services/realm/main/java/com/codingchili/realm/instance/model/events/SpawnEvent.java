package com.codingchili.realm.instance.model.events;

import com.codingchili.realm.instance.model.Entity;

/**
 * @author Robin Duda
 */
public class SpawnEvent implements Event {
    private Entity entity;

    public SpawnEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

    @Override
    public Type getType() {
        return Type.SPAWN;
    }
}
