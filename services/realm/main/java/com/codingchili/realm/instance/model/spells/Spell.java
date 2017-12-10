package com.codingchili.realm.instance.model.spells;

import com.codingchili.realm.instance.model.entity.Entity;

/**
 * @author Robin Duda
 */
public interface Spell<T> {

    /**
     * @return information about the spell.
     */
    SpellInfo metadata();

    /**
     * Invokes the spell.
     *
     * @param caster the caster
     * @param target the target
     */
    void cast(Entity caster, SpellTarget target);
}
