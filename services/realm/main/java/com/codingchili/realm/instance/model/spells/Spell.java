package com.codingchili.realm.instance.model.spells;

/**
 * @author Robin Duda
 */
public class Spell {
    protected String name = "no name";
    protected String description = "no description";
    protected boolean mobile = false;
    protected Target target;
    protected ScriptProvider onCastBegin;    // check pre-requisites - must check result.
    protected ScriptProvider onCastProgress; // implement for channeled abilities.
    protected ScriptProvider onCastComplete; // implement casted spell logic here.

    public ScriptProvider getOnCastBegin() {
        return onCastBegin;
    }

    public void setOnCastBegin(ScriptProvider onCastBegin) {
        this.onCastBegin = onCastBegin;
    }

    public ScriptProvider getOnCastProgress() {
        return onCastProgress;
    }

    public void setOnCastProgress(ScriptProvider onCastProgress) {
        this.onCastProgress = onCastProgress;
    }

    public ScriptProvider getOnCastComplete() {
        return onCastComplete;
    }

    public void setOnCastComplete(ScriptProvider onCastComplete) {
        this.onCastComplete = onCastComplete;
    }
}
