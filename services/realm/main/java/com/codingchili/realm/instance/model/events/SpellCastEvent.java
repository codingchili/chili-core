package com.codingchili.realm.instance.model.events;

import com.codingchili.realm.instance.model.spells.ActiveSpell;
import com.codingchili.realm.instance.model.spells.SpellTarget;

/**
 * @author Robin Duda
 *
 * Emitted when a new spell is casted.
 */
public class SpellCastEvent implements Event {
    private ActiveSpell spell;

    public SpellCastEvent(ActiveSpell spell) {
        this.spell = spell;
    }

    public String getSpell() {
        return spell.getSpell().getName();
    }

    public SpellCastEvent setSpell(ActiveSpell spell) {
        this.spell = spell;
        return this;
    }

    public SpellCycle getCycle() {
        return spell.getCycle();
    }

    public SpellTarget getTarget() {
        return spell.getTarget();
    }

    public Float getCastTime() {
        return spell.getSpell().getCasttime();
    }

    @Override
    public String getSource() {
        return spell.getSource().getId();
    }

    @Override
    public EventType getType() {
        return EventType.SPELL;
    }

    @Override
    public Broadcast getBroadcast() {
        return Broadcast.PARTITION;
    }
}
