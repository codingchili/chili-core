package com.codingchili.realm.instance.model;

public class SpellMetadata {
    private boolean mobile = false;
    private Target target = Target.caster;
    private Float cooldown = 1.0f;
    private Float casttime = 1.0f;
    private Float range = 100f;
    private String description;
    private String name;

    public SpellMetadata() {
    }

    public boolean isMobile() {
        return mobile;
    }

    public SpellMetadata setMobile(boolean mobile) {
        this.mobile = mobile;
        return this;
    }

    public Target getTarget() {
        return target;
    }

    public SpellMetadata setTarget(Target target) {
        this.target = target;
        return this;
    }

    public Float getCooldown() {
        return cooldown;
    }

    public SpellMetadata setCooldown(Float cooldown) {
        this.cooldown = cooldown;
        return this;
    }

    public Float getCasttime() {
        return casttime;
    }

    public SpellMetadata setCasttime(Float casttime) {
        this.casttime = casttime;
        return this;
    }

    public Float getRange() {
        return range;
    }

    public SpellMetadata setRange(Float range) {
        this.range = range;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public SpellMetadata setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getName() {
        return name;
    }

    public SpellMetadata setName(String name) {
        this.name = name;
        return this;
    }
}