package com.codingchili.realm.instance.model.events;

import com.codingchili.realm.instance.model.Spell;

/**
 * @author Robin Duda
 */
public class SpellEvent implements Event {
    private SpellCycle cycle;

    public SpellEvent(Spell spell, SpellCycle cycle) {

    }

    public SpellCycle getCycle() {
        return cycle;
    }

    @Override
    public Type getType() {
        return Type.SPELL;
    }
}
