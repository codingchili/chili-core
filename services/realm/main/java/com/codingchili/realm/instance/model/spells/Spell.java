package com.codingchili.realm.instance.model.spells;

/**
 * @author Robin Duda
 *
 * A spell item from the spell DB.
 */
public class Spell {
    protected String name = "no name";
    protected String description = "no description";
    protected Boolean mobile = false;
    protected Target target;
    protected Integer charges = 1;
    protected Integer recharge = Integer.MAX_VALUE;
    protected Integer cooldown = 1;
    protected Integer casttime = 0;
    protected Integer range = 100;
    protected Scripted onCastBegin;    // check pre-requisites - must check result.
    protected Scripted onCastProgress; // implement for channeled abilities.
    protected Scripted onCastComplete; // implement casted spell logic here.

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

    public Boolean getMobile() {
        return mobile;
    }

    public void setMobile(Boolean mobile) {
        this.mobile = mobile;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public Integer getCharges() {
        return charges;
    }

    public void setCharges(Integer charges) {
        this.charges = charges;
    }

    public Integer getRecharge() {
        return recharge;
    }

    public void setRecharge(Integer recharge) {
        this.recharge = recharge;
    }

    public Integer getCooldown() {
        return cooldown;
    }

    public void setCooldown(Integer cooldown) {
        this.cooldown = cooldown;
    }

    public Integer getCasttime() {
        return casttime;
    }

    public void setCasttime(Integer casttime) {
        this.casttime = casttime;
    }

    public Integer getRange() {
        return range;
    }

    public void setRange(Integer range) {
        this.range = range;
    }

    public Scripted getOnCastBegin() {
        return onCastBegin;
    }

    public void setOnCastBegin(Scripted onCastBegin) {
        this.onCastBegin = onCastBegin;
    }

    public Scripted getOnCastProgress() {
        return onCastProgress;
    }

    public void setOnCastProgress(Scripted onCastProgress) {
        this.onCastProgress = onCastProgress;
    }

    public Scripted getOnCastComplete() {
        return onCastComplete;
    }

    public void setOnCastComplete(Scripted onCastComplete) {
        this.onCastComplete = onCastComplete;
    }
}
