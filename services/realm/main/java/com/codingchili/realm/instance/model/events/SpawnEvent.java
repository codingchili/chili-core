package com.codingchili.realm.instance.model.events;

import com.codingchili.realm.instance.model.entity.Creature;
import com.codingchili.realm.instance.model.entity.Entity;

/**
 * @author Robin Duda
 */
public class SpawnEvent implements Event {
    private SpawnType type = SpawnType.SPAWN;
    private Creature creature;

    public SpawnEvent setType(SpawnType type) {
        this.type = type;
        return this;
    }

    public SpawnEvent setEntity(Entity entity) {
        this.creature = creature;
        return this;
    }

    public Creature getCreature() {
        return creature;
    }

    @Override
    public EventType getType() {
        return EventType.SPAWN;
    }

    public enum SpawnType {SPAWN, DESPAWN}
}
