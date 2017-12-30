package com.codingchili.realm.instance.model.entity;

import com.codingchili.realm.instance.model.items.ArmorType;
import com.codingchili.realm.instance.model.items.WeaponType;
import com.codingchili.realm.instance.model.stats.Stats;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Robin Duda
 *
 * A playable class, a character template.
 */
public class PlayableClass {
    private String name = "default";
    private String description = "description";
    private Stats stats;
    private Collection<String> spells = new ArrayList<>();
    private Collection<String> keywords = new ArrayList<>();
    private Collection<WeaponType> weapons = new ArrayList<>();
    private Collection<ArmorType> armors = new ArrayList<>();

    public String getName() {
        return name;
    }

    public PlayableClass setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public PlayableClass setDescription(String description) {
        this.description = description;
        return this;
    }

    public Collection<String> getSpells() {
        return spells;
    }

    public PlayableClass setSpells(Collection<String> spells) {
        this.spells = spells;
        return this;
    }

    public Collection<String> getKeywords() {
        return keywords;
    }

    public PlayableClass setKeywords(Collection<String> keywords) {
        this.keywords = keywords;
        return this;
    }

    public Collection<WeaponType> getWeapons() {
        return weapons;
    }

    public PlayableClass setWeapons(Collection<WeaponType> weapons) {
        this.weapons = weapons;
        return this;
    }

    public Collection<ArmorType> getArmors() {
        return armors;
    }

    public PlayableClass setArmors(Collection<ArmorType> armors) {
        this.armors = armors;
        return this;
    }

    public Stats getStats() {
        return stats;
    }

    public PlayableClass setStats(Stats stats) {
        this.stats = stats;
        return this;
    }
}
