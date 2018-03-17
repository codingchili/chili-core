package com.codingchili.realm.instance.model.afflictions;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.entity.Creature;
import com.codingchili.realm.instance.model.spells.DamageType;
import com.codingchili.realm.instance.scripting.Bindings;
import com.codingchili.realm.instance.model.stats.Attribute;
import com.codingchili.realm.instance.model.stats.Stats;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.codingchili.core.logging.Level;

/**
 * @author Robin Duda
 * <p>
 * Maps an active affliction onto an entity.
 */
public class ActiveAffliction {
    private Map<String, Object> state = new HashMap<>(); // used by scripts to store data.
    private Stats stats = new Stats();
    private Affliction affliction;
    private Creature source;
    private Creature target;
    private Integer ticks;
    private Integer interval;
    private Long start = System.currentTimeMillis();

    public ActiveAffliction(Creature source, Creature target, Affliction affliction) {
        this.source = source;
        this.target = target;
        this.affliction = affliction;
        this.ticks = GameContext.secondsToTicks(affliction.duration);
        this.interval = GameContext.secondsToTicks(affliction.interval);

        System.out.println("created affliction for " + ticks + " number of ticks.");
    }

    public Stats modify(GameContext context) {
        stats.clear();
        affliction.apply(getBindings(context).setStats(stats));
        return stats;
    }

    @JsonIgnore
    public Long getStart() {
        return start;
    }

    /**
     * @param context the game context that the target exists within.
     * @return true if still active.
     */
    public boolean tick(GameContext context) {
        affliction.tick(getBindings(context));
        return ((ticks -= interval) > 0);
    }


    public boolean shouldTick(long currentTick) {
        return (currentTick % interval) == 0;
    }

    private Bindings getBindings(GameContext context) {
        return new Bindings()
                .setContext(context)
                .setState(state)
                .set("spells", context.spells())
                .set("this", this)
                .set("DamageType", DamageType.class)
                .set("log", (Consumer<String>) (message) -> {
                    context.getLogger(getClass()).event("affliction", Level.INFO)
                            .put("name", affliction.getName())
                            .send(message);
                })
                .setAttribute(Attribute.class);
    }

    public Affliction getAffliction() {
        return affliction;
    }

    public void setAffliction(Affliction affliction) {
        this.affliction = affliction;
    }

    public Creature getSource() {
        return source;
    }

    public Creature getTarget() {
        return target;
    }
}
