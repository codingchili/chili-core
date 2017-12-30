package com.codingchili.realm.instance.model.stats;

/**
 * @author Robin Duda
 */
public enum Attribute {
    level,  // only for base stats
    experience,  // only for base stats
    maxhealth, // computed
    health, // computed
    movement,  // computed
    haste, // computed: casting time and cooldown reduction.
    attackspeed, // computed
    constitution, // base stat
    dexterity,  // base stat
    strength,  // base stat
    intelligence,  // base stat
    wisdom,  // base stat
    magicResist,  // base stat
    armorClass,  // base stat
    attackpower, // computed by STR
    spellpower,  // computed by INT
    healing, // affects the efficiency of heals.
    energy,
    maxenergy

    /*
    todo:
    - calculate health
    - calculate attack power
    - calculate spell power
    - calculate max health
    - set health to max health on character creation.

    defaults
    - set healing to 1
    - set haste to 1
    - set movement to base movement value.

    apply damage/heals using a method call in SpellEngine
        which calculates armor/magic resist reduction
        healing amplifier etc.
     */
}
