package com.codingchili.realm.instance.model.stats;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Robin Duda
 */
public class Stats extends ConcurrentHashMap<Attribute, Float> {

    public Stats() {
        super();
    }

    public Stats update(Attribute type, float points) {
        float current = getOrDefault(type, 0f) + points;
        put(type, current);
        return this;
    }

    public Stats set(Attribute type, float value) {
        put(type, value);
        return this;
    }

    public float get(Attribute attribute) {
        return getOrDefault(attribute, 0f);
    }

    public Stats apply(Stats stats) {
        stats.forEach(this::update);
        return this;
    }
}
