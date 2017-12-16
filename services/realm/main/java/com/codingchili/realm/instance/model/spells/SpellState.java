package com.codingchili.realm.instance.model.spells;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Robin Duda
 */
public class SpellState {
    private static final Integer GCD_MS = 500;
    private Map<String, Spell> spells = new HashMap<>();
    private Long gcd = System.currentTimeMillis();

    public void casted(Spell spell) {
        gcd = System.currentTimeMillis() + GCD_MS;
    }

    public boolean cooldown(Spell spell) {
        if (System.currentTimeMillis() < gcd) {
            return false;
        }
        return true;
    }

    public Map<String, Spell> getSpells() {
        return spells;
    }

    public void setSpells(Map<String, Spell> spells) {
        this.spells = spells;
    }
}
