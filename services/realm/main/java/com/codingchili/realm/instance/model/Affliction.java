package com.codingchili.realm.instance.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author Robin Duda
 * Describes an affliction, loaded from configurations.
 * <p>
 * Afflictions affects a character by modifying their attributes temporarily.
 */
public class Affliction implements Serializable {
    private String name;
    private String description;
    private Target target;
    private Double rate;
    private Double chance;
    private Double duration;
    private Boolean permanent;
    private ArrayList<Modifier> modifier;
    private ArrayList<Trigger> trigger;
    private ArrayList<Affliction> affliction;

    public Boolean getPermanent() {
        return permanent;
    }

    public void setPermanent(Boolean permanent) {
        this.permanent = permanent;
    }

    public ArrayList<Affliction> getAffliction() {
        return affliction;
    }

    public void setAffliction(ArrayList<Affliction> affliction) {
        this.affliction = affliction;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public Boolean isPermanent() {
        return permanent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<Modifier> getModifier() {
        return modifier;
    }

    public void setModifier(ArrayList<Modifier> modifier) {
        this.modifier = modifier;
    }

    public ArrayList<Trigger> getTrigger() {
        return trigger;
    }

    public void setTrigger(ArrayList<Trigger> trigger) {
        this.trigger = trigger;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public Double getChance() {
        return chance;
    }

    public void setChance(Double chance) {
        this.chance = chance;
    }
}
