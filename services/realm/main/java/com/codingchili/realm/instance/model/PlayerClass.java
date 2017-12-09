package com.codingchili.realm.instance.model;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Robin Duda
 */
public class PlayerClass {
    private String name = "default";
    private String description = "description";
    private Collection<SpellMetadata> spells = new ArrayList<>();
    private Collection<String> keywords = new ArrayList<>();
    private Collection<String> weapons = new ArrayList<>();

    public String getName() {
        return name;
    }

    public PlayerClass setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public PlayerClass setDescription(String description) {
        this.description = description;
        return this;
    }

    public Collection<SpellMetadata> getSpells() {
        return spells;
    }

    public PlayerClass setSpells(Collection<SpellMetadata> spells) {
        this.spells = spells;
        return this;
    }

    public Collection<String> getKeywords() {
        return keywords;
    }

    public PlayerClass setKeywords(Collection<String> keywords) {
        this.keywords = keywords;
        return this;
    }

    public Collection<String> getWeapons() {
        return weapons;
    }

    public PlayerClass setWeapons(Collection<String> weapons) {
        this.weapons = weapons;
        return this;
    }
}
