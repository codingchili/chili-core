package com.codingchili.realm.instance.model;

import java.util.ArrayList;

import com.codingchili.core.configuration.AttributeConfigurable;

/**
 * @author Robin Duda
 *         model for player classes.
 */
public class PlayerClass extends AttributeConfigurable {
    private String name;
    private String description;
    private ArrayList<Spell> spells;
    private ArrayList<String> keywords;
    private ArrayList<String> weapons;

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

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<Spell> getSpells() {
        return spells;
    }

    public void setSpells(ArrayList<Spell> spells) {
        this.spells = spells;
    }

    public ArrayList<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(ArrayList<String> keywords) {
        this.keywords = keywords;
    }

    public ArrayList<String> getWeapons() {
        return weapons;
    }

    public void setWeapons(ArrayList<String> weapons) {
        this.weapons = weapons;
    }
}
