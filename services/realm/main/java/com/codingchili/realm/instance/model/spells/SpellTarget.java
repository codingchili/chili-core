package com.codingchili.realm.instance.model.spells;

import com.codingchili.realm.instance.model.entity.Creature;
import com.codingchili.realm.instance.model.entity.Vector;

import java.util.Optional;

/**
 * A spelltarget.
 */
public class SpellTarget {
    private Vector vector;
    private Creature creature;

    public Optional<Vector> getVector() {
        return Optional.ofNullable(vector);
    }

    public void setVector(Vector vector) {
        this.vector = vector;
    }

    public Optional<Creature> getCreature() {
        return Optional.ofNullable(creature);
    }

    public void setCreature(Creature creature) {
        this.creature = creature;
    }
}
