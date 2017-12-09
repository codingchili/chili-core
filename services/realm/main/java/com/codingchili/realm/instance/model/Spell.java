package com.codingchili.realm.instance.model;

/**
 * @author Robin Duda
 */
public interface Spell<T> {

    /**
     * @return information about the spell.
     */
    SpellMetadata metadata();

    /**
     * Invokes the spell.
     *
     * @param caster the caster
     * @param target the target
     */
    void cast(Entity caster, SpellTarget target);
}
