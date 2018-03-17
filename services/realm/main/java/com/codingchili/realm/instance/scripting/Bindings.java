package com.codingchili.realm.instance.scripting;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.afflictions.Affliction;
import com.codingchili.realm.instance.model.entity.Creature;
import com.codingchili.realm.instance.model.stats.Attribute;
import com.codingchili.realm.instance.model.stats.Stats;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Robin Duda
 *
 * Bindings used when calling scripts.
 *
 * For non native scripts the map is passed as it is.
 * For native scripts this class provides typed access to binding variables.
 */
public class Bindings extends HashMap<String, Object> {
    private static final String CONTEXT = "context";
        private static final String ATTRIBUTE = "Attribute";
    private static final String AFFLICTION = "affliction";
    private static final String STATS = "stats";
    private static final String SOURCE = "source";
    private static final String TARGET = "target";
    private static final String STATE = "state";

    public Bindings() {}

    public Bindings(Map<String, Object> map) {
        map.forEach(this::put);
    }

    public Bindings setContext(GameContext context) {
        put(CONTEXT, context);
        return this;
    }

    public Bindings setAttribute(Class<Attribute> attribute) {
        put(ATTRIBUTE, attribute);
        return this;
    }

    public Bindings setAffliction(Affliction affliction) {
        put(AFFLICTION, affliction);
        return this;
    }

    public Bindings setStats(Stats stats) {
        put(STATS, stats);
        return this;
    }

    public Stats getStats() {
        return (Stats) get(STATS);
    }

    public GameContext getContext() {
        return (GameContext) get(CONTEXT);
    }

    public Affliction getAffliction() {
        return (Affliction) get(AFFLICTION);
    }

    public Bindings setSource(Creature source) {
        put(SOURCE, source);
        return this;
    }

    public Bindings setTarget(Creature target) {
        put(TARGET, target);
        return this;
    }

    public Creature getSource() {
        return (Creature) get(SOURCE);
    }

    public Creature getTarget() {
        return (Creature) get(TARGET);
    }

    public Bindings setState(Map<String,Object> state) {
        put(STATE, state);
        return this;
    }

    public Bindings set(String name, Object value) {
        put(name, value);
        return this;
    }
}
