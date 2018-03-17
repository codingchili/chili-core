package com.codingchili.realm.instance.model.spells;

import com.codingchili.realm.instance.model.entity.Creature;
import com.codingchili.realm.instance.model.entity.Vector;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A spelltarget.
 */
public class SpellTarget {
    private Vector vector;
    private Creature creature;

    public Vector getVector() {
        return vector;
    }

    public SpellTarget setVector(Vector vector) {
        this.vector = vector;
        return this;
    }

    @JsonIgnore
    public Creature getCreature() {
        return creature;
    }

    public String getTargetId() {
        return creature.getId();
    }

    public SpellTarget setCreature(Creature creature) {
        this.creature = creature;
        return this;
    }
}
