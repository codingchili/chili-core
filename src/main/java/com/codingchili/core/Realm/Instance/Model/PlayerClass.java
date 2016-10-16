package com.codingchili.core.Realm.Instance.Model;

import com.codingchili.core.Configuration.LoadableConfigurable;
import com.codingchili.core.Configuration.Strings;

import java.io.Serializable;
import java.util.ArrayList;

import static com.codingchili.core.Configuration.Strings.EXT_JSON;
import static com.codingchili.core.Configuration.Strings.PATH_GAME_CLASSES;

/**
 * @author Robin Duda
 *         Model for player classes.
 */
public class PlayerClass implements Serializable, LoadableConfigurable {
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

    @Override
    public String getPath() {
        return PATH_GAME_CLASSES + name + EXT_JSON;
    }
}
