package com.codingchili.realm.instance.model.spells;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.entity.Entity;
import com.codingchili.realm.instance.model.stats.Attribute;
import com.codingchili.realm.instance.model.stats.Stats;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Robin Duda
 * <p>
 * Maps an active affliction onto an entity.
 */
public class ActiveAffliction {
    private Affliction affliction;
    private Entity source;
    private Entity target;
    private Integer ticks;

    public ActiveAffliction(Entity source, Entity target, Affliction affliction) {
        this.source = source;
        this.target = target;
        this.affliction = affliction;
        this.ticks = affliction.duration;
    }

    public Stats modify(GameContext context) {
        Stats stats = new Stats();
        Map<String, Object> bindings = getBindings(context);
        bindings.put("stats", stats);
        affliction.apply(bindings);
        return stats;
    }

    /**
     * @param context the game context that the target exists within.
     * @return true if still active.
     */
    public boolean tick(GameContext context) {
        System.out.println(affliction.tick(getBindings(context)));
        return (--ticks > 0);
    }

    private Map<String, Object> getBindings(GameContext context) {
        Map<String, Object> bindings = new HashMap<>();
        bindings.put("source", source);
        bindings.put("target", target);
        bindings.put("context", context);
        bindings.put("attribute", Attribute.class);
        bindings.put("spell", context.getSpellEngine());
        bindings.put("grid", context.getGrid());
        return bindings;
    }

    public Affliction getAffliction() {
        return affliction;
    }

    public void setAffliction(Affliction affliction) {
        this.affliction = affliction;
    }

    public Entity getSource() {
        return source;
    }

    public void setSource(Entity source) {
        this.source = source;
    }

    public Entity getTarget() {
        return target;
    }

    public void setTarget(Entity target) {
        this.target = target;
    }
}
