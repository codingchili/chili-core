package com.codingchili.realm.instance.model.events;

import com.codingchili.realm.instance.model.entity.Entity;

/**
 * @author Robin Duda
 */
public class SpawnEvent implements Event {
    private SpawnType type = SpawnType.SPAWN;
    private Entity entity;

    public SpawnEvent setType(SpawnType type) {
        this.type = type;
        return this;
    }

    public SpawnEvent setEntity(Entity entity) {
        this.entity = entity;
        return this;
    }

    public Entity getEntity() {
        return entity;
    }

    public SpawnType getSpawn() {
        return type;
    }

    @Override
    public EventType getType() {
        return EventType.SPAWN;
    }

    public enum SpawnType {SPAWN, DESPAWN, DISCONNECT, DEATH}
}
