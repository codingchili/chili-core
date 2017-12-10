package com.codingchili.realm.instance.model.stats;

import java.util.HashMap;

/**
 * @author Robin Duda
 */
public class Modifiers extends HashMap<Attribute, Float> {

    public Modifiers add(Attribute attribute, Float value) {
        Float current = getOrDefault(attribute, 1.0f);
        put(attribute, current + value);
        return this;
    }

    public Modifiers set(Attribute attribute, Float value) {
        put(attribute, value);
        return this;
    }

    public Float get(Attribute attribute) {
        return getOrDefault(attribute, 1.0f);
    }

    public Stats apply(Stats stats) {
        for (Attribute attribute : keySet()) {
            if (stats.containsKey(attribute)) {
                stats.set(attribute, Math.round(stats.get(attribute) * get(attribute)));
            }
        }
        return stats;
    }
}
