package com.codingchili.realm.instance.model;

import java.io.Serializable;
import java.util.List;

/**
 * @author Robin Duda
 *         A spell that may be cast by a player or NPC.
 */
class Spell implements Serializable {
    private String name;
    private String description;
    private Target target;
    private Double cooldown;
    private Double casttime;
    private Cost cost;
    private List<Modifier> modifier;
    private List<Affliction> afflictions;
    private Boolean passive = false;

    public Cost getCost() {
        return cost;
    }

    public void setCost(Cost cost) {
        this.cost = cost;
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

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public Double getCooldown() {
        return cooldown;
    }

    public void setCooldown(Double cooldown) {
        this.cooldown = cooldown;
    }

    public Double getCasttime() {
        return casttime;
    }

    public void setCasttime(Double casttime) {
        this.casttime = casttime;
    }

    public List<Modifier> getModifier() {
        return modifier;
    }

    public void setModifier(List<Modifier> modifier) {
        this.modifier = modifier;
    }

    public Boolean isPassive() {
        return passive;
    }

    public void setPassive(Boolean passive) {
        this.passive = passive;
    }

    public List<Affliction> getAfflictions() {
        return afflictions;
    }

    public void setAfflictions(List<Affliction> afflictions) {
        this.afflictions = afflictions;
    }
}
