package com.codingchili.realm.instance.model.events;

import com.codingchili.realm.instance.model.entity.Creature;
import com.codingchili.realm.instance.model.spells.ActiveSpell;

import java.util.Optional;

/**
 * @author Robin Duda
 *
 * Emitted when a new spell is casted.
 */
public class SpellCastEvent implements Event {
    private ActiveSpell spell;

    public SpellCastEvent() {}

    public SpellCastEvent(ActiveSpell spell) {
        this.spell = spell;
    }

    public ActiveSpell getSpell() {
        return spell;
    }

    public SpellCastEvent setSpell(ActiveSpell spell) {
        this.spell = spell;
        return this;
    }

    @Override
    public Optional<Creature> getSource() {
        return Optional.of(spell.getCaster());
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
