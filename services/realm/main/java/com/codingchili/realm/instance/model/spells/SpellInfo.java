package com.codingchili.realm.instance.model.spells;

/**
 * @author Robin Duda
 *
 * Info about a spell, this is the information that is sent to clients.
 */
public class SpellInfo {
    private String name;
    private String description;
    private Target target = Target.caster;
    private Integer cooldown = 1;
    private Integer casttime = 1;
    private Integer range = 100;
    private Integer charges = 1;
    private Integer recharge = 500;
    private boolean mobile = false;

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

    public Integer getCooldown() {
        return cooldown;
    }

    public SpellInfo setCooldown(Integer cooldown) {
        this.cooldown = cooldown;
        return this;
    }

    public Integer getCasttime() {
        return casttime;
    }

    public SpellInfo setCasttime(Integer casttime) {
        this.casttime = casttime;
        return this;
    }

    public Integer getRange() {
        return range;
    }

    public SpellInfo setRange(Integer range) {
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
}
