package com.codingchili.realm.instance.model.afflictions;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.entity.Creature;
import com.codingchili.realm.instance.scripting.Bindings;
import com.codingchili.realm.instance.scripting.Scripted;

import java.util.Random;

import com.codingchili.core.storage.Storable;

/**
 * @author Robin Duda
 */
public class Affliction implements Storable {
    protected String name = "missing name";
    protected String description = "missing description";
    protected Float duration = 8.0f;
    protected Float interval = 2.0f;
    protected Float chance = 1.0f;
    protected Scripted modifier;
    protected Scripted tick;

    public ActiveAffliction apply(Creature source, Creature target) {
        return new ActiveAffliction(source, target, this);
    }

    public String getName() {
        return name;
    }

    @Override
    public String getId() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public <T> T tick(Bindings bindings) {
        if (tick != null && interval > 0) {
            if (chance == 1.0f || new Random().nextFloat() < chance) {
                return tick.apply(bindings);
            }
        }
        return null;
    }

    public <T> T apply(Bindings bindings) {
        if (modifier != null) {
            return modifier.apply(bindings);
        }
        return null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Float getDuration() {
        return duration;
    }

    public void setDuration(Float duration) {
        this.duration = duration;
    }

    public Float getChance() {
        return chance;
    }

    public void setChance(Float chance) {
        this.chance = chance;
    }

    public Scripted getModifier() {
        return modifier;
    }

    public void setModifier(Scripted modifier) {
        this.modifier = modifier;
    }

    public Scripted getTick() {
        return tick;
    }

    public void setTick(Scripted tick) {
        this.tick = tick;
    }

    public Float getInterval() {
        return interval;
    }

    public void setInterval(Float interval) {
        this.interval = interval;
    }
}
