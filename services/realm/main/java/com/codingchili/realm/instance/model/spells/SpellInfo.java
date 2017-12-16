package com.codingchili.realm.instance.model.spells;

public class SpellInfo {
    private String name;
    private String description;
    private Target target = Target.caster;
    private Float cooldown = 1.0f;
    private Float casttime = 1.0f;
    private Float range = 100f;
    private boolean mobile = false;
    private Scripted casted;

    public void apply(Bindings bindings) {
        casted.apply(bindings);
    }

    public boolean isMobile() {
        return mobile;
    }

    public SpellInfo setMobile(boolean mobile) {
        this.mobile = mobile;
        return this;
    }

    public Target getTarget() {
        return target;
    }

    public SpellInfo setTarget(Target target) {
        this.target = target;
        return this;
    }

    public Float getCooldown() {
        return cooldown;
    }

    public SpellInfo setCooldown(Float cooldown) {
        this.cooldown = cooldown;
        return this;
    }

    public Float getCasttime() {
        return casttime;
    }

    public SpellInfo setCasttime(Float casttime) {
        this.casttime = casttime;
        return this;
    }

    public Float getRange() {
        return range;
    }

    public SpellInfo setRange(Float range) {
        this.range = range;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public SpellInfo setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getName() {
        return name;
    }

    public SpellInfo setName(String name) {
        this.name = name;
        return this;
    }

    public Scripted getCasted() {
        return casted;
    }

    public void Scripted(Scripted casted) {
        this.casted = casted;
    }
}
