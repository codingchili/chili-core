package com.codingchili.realm.instance.model;

/**
 * @author Robin Duda
 */
public class Affliction {
    private String name;
    private String description;
    private Double duration;
    private Double rage;
    private Double chance;

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

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public Double getRage() {
        return rage;
    }

    public void setRage(Double rage) {
        this.rage = rage;
    }

    public Double getChance() {
        return chance;
    }

    public void setChance(Double chance) {
        this.chance = chance;
    }
}
