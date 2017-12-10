package com.codingchili.realm.instance.model.spells;

import com.codingchili.realm.instance.model.entity.Entity;

import java.util.Map;

/**
 * @author Robin Duda
 */
public class Affliction {
    protected String name = "missing name";
    protected String description = "missing description";
    protected Integer duration = 30;
    protected Integer interval = 10;
    protected Float chance = 1.0f;
    protected Scripted modifier;
    protected Scripted tick;

    public ActiveAffliction apply(Entity source, Entity target) {
        return new ActiveAffliction(source, target, this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object tick(Map<String, ?> bindings) {
        if (tick != null) {
            return tick.eval(bindings);
        }
        return null;
    }

    public Object apply(Map<String, ?> bindings) {
        if (modifier != null) {
            return modifier.eval(bindings);
        }
        return null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Float getChance() {
        return chance;
    }

    public void setChance(Float chance) {
        this.chance = chance;
    }

    public String getModifier() {
        return modifier.getSource();
    }

    public void setModifier(String modifier) {
        this.modifier = ScriptEngine.script(modifier);
    }

    public String getTick() {
        return tick.getSource();
    }

    public void setTick(String tick) {
        this.tick = ScriptEngine.script(tick);
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }
}
