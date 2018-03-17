package com.codingchili.realm.instance.model.spells;

import com.codingchili.realm.instance.context.GameContext;

import java.util.*;

/**
 * @author Robin Duda
 * <p>
 * Spell state that is stored on creatures.
 */
public class SpellState {
    private static final Integer GCD_MS = 250; // todo: externalize.
    private Set<String> learned = new HashSet<>();
    private Map<String, Long> casted = new HashMap<>();
    private Map<String, Integer> charges = new HashMap<>();
    private Long gcd = 0L;

    public void setCooldown(Spell spell) {
        casted.put(spell.getId(), System.currentTimeMillis());
        gcd = System.currentTimeMillis() + GCD_MS;
    }

    public boolean cooldown(Spell spell) {
        if (!gcd() && charges(spell)) {
            Long last = casted.getOrDefault(spell.getId(), 0L);
            Long lastCDEnd = (long) (last + (spell.getCooldown() * 1000));
            return (System.currentTimeMillis() < lastCDEnd);
        }
        return true;
    }

    private boolean gcd() {
        return System.currentTimeMillis() < gcd;
    }

    private boolean charges(Spell spell) {
        if (spell.charges == 1) {
            return true; // do not care about charges if non-recharging.
        }
        Integer count = charges.get(spell.getId());
        return (count == null || count > 0);
    }

    public void tick(SpellDB spells, long currentTick) {
        for (String spellName: learned) {
            Spell spell = spells.getByName(spellName);
            int cooldown = GameContext.secondsToTicks(spell.getCooldown());

            if (spell.charges > 1 && currentTick % cooldown == 0) {
                charges.compute(spellName, (key, charges) -> {
                    if (charges < spell.charges) {
                        return charges + 1;
                    } else {
                        return charges;
                    }
                });
            }
        }
    }

    public Collection<String> getLearned() {
        return learned;
    }

    public SpellState setLearned(String spellName) {
        learned.add(spellName);
        return this;
    }

    public SpellState setNotLearned(String spellName) {
        learned.remove(spellName);
        return this;
    }

    public boolean learned(String spell) {
        return learned.contains(spell);
    }
}
