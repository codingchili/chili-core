package com.codingchili.realm.instance.model.spells;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.scripting.Scripted;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.codingchili.core.configuration.Configurable;
import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.storage.Storable;

/**
 * @author Robin Duda
 *
 * A spell item from the spell DB.
 */
public class Spell implements Storable, Configurable {
    protected String name = "no name";
    protected String description = "no description";
    protected Boolean mobile = true;
    protected Target target = Target.caster;
    protected Integer charges = 1;
    protected Integer recharge = Integer.MAX_VALUE;
    protected Integer cooldown = 1;
    protected Integer casttime = 0;
    protected Integer range = 100;
    protected Integer active = 0;
    protected Integer tick = GameContext.secondsToTicks(0.5);
    protected Scripted onCastBegin;    // check pre-requisites - must check result.
    protected Scripted onCastProgress; // implement for channeled abilities.
    protected Scripted onCastComplete; // implement casted spell logic here.
    protected Scripted onSpellEffect;    // for spells that are active longer than the casting period.

    @Override
    public String getPath() {
        return "conf/game/classes/" + name + CoreStrings.EXT_YAML;
    }

    public String getId() {
        return name;
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

    @JsonIgnore
    public Integer getActive() {
        return active;
    }

    public void setActive(Integer active) {
        this.active = active;
    }

    @JsonIgnore
    public Integer getTick() {
        return tick;
    }

    public void setTick(Integer tick) {
        this.tick = tick;
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

    public Scripted getOnSpellEffect() {
        return onSpellEffect;
    }

    public void setOnSpellEffect(Scripted onSpellEffect) {
        this.onSpellEffect = onSpellEffect;
    }

    public static void main(String[] args) {
        System.out.println(Serializer.yaml(new Spell()));
    }
}
