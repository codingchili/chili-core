package com.codingchili.realm.instance.model.spells;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.afflictions.Affliction;
import com.codingchili.realm.instance.model.entity.Grid;
import com.codingchili.realm.instance.model.stats.Attribute;
import com.codingchili.realm.instance.model.stats.Stats;

import java.util.HashMap;

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
    private static final String GRID = "GRID";
    private static final String ATTRIBUTE = "ATTRIBUTE";
    private static final String AFFLICTION = "AFFLICTION";
    public static final String STATS = "STATS";

    public Bindings setContext(GameContext context) {
        put(CONTEXT, context);
        return this;
    }

    public Bindings setAttribute(Class<Attribute> attribute) {
        put(ATTRIBUTE, attribute);
        return this;
    }

    public Bindings setGrid(Grid grid) {
        put(GRID, grid);
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


    public Grid getGrid() {
        return (Grid) get(GRID);
    }

    public Affliction getAffliction() {
        return (Affliction) get(AFFLICTION);
    }
}
