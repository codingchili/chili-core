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
