package com.codingchili.realm.instance.model.spells;

import java.util.*;

/**
 * @author Robin Duda
 * <p>
 * Spell state that is stored on creatures.
 */
public class SpellState {
    private static final Integer GCD_MS = 250;
    private Collection<String> learned = new ArrayList<>();
    private Map<String, Long> casted = new HashMap<>();
    private Map<String, Integer> charges = new HashMap<>();
    private Long gcd = System.currentTimeMillis();

    public void casted(Spell spell) {
        casted.put(spell.getId(), System.currentTimeMillis());
        gcd = System.currentTimeMillis() + GCD_MS;
    }

    public boolean cooldown(Spell spell) {
        if (!gcd() && charges(spell)) {
            Long last = casted.get(spell.getId());
            return (last < System.currentTimeMillis() - spell.getCooldown());
        }
        return true;
    }

    private boolean gcd() {
        return System.currentTimeMillis() > gcd;
    }

    private boolean charges(Spell spell) {
        if (spell.recharge == Integer.MAX_VALUE) {
            return true; // do not care about charges if non-recharging.
        }
        Integer count = charges.get(spell.getId());
        return (count != null && count > 0);
    }

    public void tick() {
        // todo: process individual spell cooldowns
        // todo: process spell charges.
    }

    public Collection<String> getLearned() {
        return learned;
    }

    public void setLearned(Collection<String> learned) {
        this.learned = learned;
    }

    public boolean learned(String spell) {
        return learned.contains(spell);
    }
}
