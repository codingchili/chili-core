package com.codingchili.realm.instance.model.spells;

import java.util.*;

/**
 * @author Robin Duda
 *
 * Container of all registered spells.
 */
public class SpellBase {
    private Map<String, Spell> spells = new HashMap<>();
    // todo: load spells from yaml.

    public Optional<Spell> getByName(String spell) {
        return Optional.ofNullable(spells.getOrDefault(spell, null));
    }

    public Collection<Spell> list() {
        return spells.values();
    }
}
