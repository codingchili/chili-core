package com.codingchili.realm.instance.model.spells;

import com.codingchili.realm.instance.scripting.Scripted;

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
    protected Boolean mobile = true; // can move and cast?
    protected Target target = Target.caster; // spell target: caster, area etc.
    protected Integer charges = 1;  // number of times the spell can be cast in a sequence without recharge.
    protected Integer range = 100; // how far away the target may be.
    protected Float interval = 0.5f; // how often to call onProgress and onEffects.
    protected Float cooldown = 1.0f; // time to regenerate a charge.
    protected Float casttime = 0.0f; // the time taken to cast the spell.
    protected Float active = 0.0f; // how long the spell is active after casting is completed.
    protected Scripted onCastBegin;    // check pre-requisites - must check result.
    protected Scripted onCastProgress; // implement for channeled abilities.
    protected Scripted onCastComplete; // implement casted spell logic here.
    protected Scripted onSpellActive;    // for spells that are active longer than the casting period.

    @Override
    public String getPath() {
        return "conf/game/classes/" + name + CoreStrings.EXT_YAML;
    }

    @Override
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

    public Float getCooldown() {
        return cooldown;
    }

    public void setCooldown(Float cooldown) {
        this.cooldown = cooldown;
    }

    public Float getCasttime() {
        return casttime;
    }

    public void setCasttime(Float casttime) {
        this.casttime = casttime;
    }

    public Integer getRange() {
        return range;
    }

    public void setRange(Integer range) {
        this.range = range;
    }

    public Float getActive() {
        return active;
    }

    public void setActive(Float active) {
        this.active = active;
    }

    public Float getInterval() {
        return interval;
    }

    public void setInterval(Float interval) {
        this.interval = interval;
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

    public Scripted getOnSpellActive() {
        return onSpellActive;
    }

    public void setOnSpellActive(Scripted onSpellActive) {
        this.onSpellActive = onSpellActive;
    }

    public static void main(String[] args) {
        System.out.println(Serializer.yaml(new Spell()));
    }
}
