package com.codingchili.realm.instance.model;

import java.util.Collection;

/**
 * @author Robin Duda
 */
public interface PlayerClass {
    String getName();

    String getDescription();

    Collection<SpellMetadata> getSpells();

    Collection<String> getKeywords();

    Collection<String> getWeapons();
}
