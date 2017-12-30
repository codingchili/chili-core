package com.codingchili.realm.instance.model.spells;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.entity.Creature;
import com.codingchili.realm.instance.model.events.SpellCycle;
import com.codingchili.realm.instance.scripting.Bindings;

/**
 * @author Robin Duda
 *
 * A spell that is being casted or has been casted.
 */
public class ActiveSpell {
    private static final String TICK = "tick";
    private static final String CASTER = "caster";
    private static final String TARGET = "target";
    private static final String GAME = "game";
    private static final String ACTIVE = "active";
    private SpellCycle cycle = SpellCycle.CASTING;
    private int progress = 0;
    private int timer;
    private Creature caster;
    private SpellTarget target;
    private Spell spell;

    public boolean completed() {
        return (--progress <= 0);
    }

    public boolean active() {
        return (--timer <= 0);
    }

    public void onCastProgress(GameContext game, int tick) {
        if (spell.onCastProgress != null) {
            Bindings bindings = getBindings(game);
            bindings.put(TICK, tick);
            spell.onCastProgress.apply(bindings);
        }
    }

    public void onCastCompleted(GameContext game) {
        if (spell.onCastComplete != null) {
            spell.onCastComplete.apply(getBindings(game));
        }
    }

    public void onCastBegin(GameContext game) {
        if (spell.onCastBegin != null) {
            spell.onCastBegin.apply(getBindings(game));
        }
    }

    public void onSpellEffects(GameContext game, int tick) {
        if (spell.onSpellEffect != null) {
            Bindings bindings = getBindings(game);
            bindings.put(TICK, tick);
            spell.onSpellEffect.apply(bindings);
        }
    }

    private Bindings getBindings(GameContext game) {
        Bindings bindings = new Bindings();
        bindings.put(CASTER, caster);
        bindings.put(TARGET, target);
        bindings.put(GAME, game);
        bindings.put(ACTIVE, this);
        return bindings;
    }

    public Spell getSpell() {
        return spell;
    }

    public ActiveSpell setSpell(Spell spell) {
        this.timer = spell.getActive();
        this.progress = spell.getCasttime();
        this.spell = spell;
        return this;
    }

    public Creature getCaster() {
        return caster;
    }

    public ActiveSpell setCaster(Creature caster) {
        this.caster = caster;
        return this;
    }

    public SpellTarget getTarget() {
        return target;
    }

    public ActiveSpell setTarget(SpellTarget target) {
        this.target = target;
        return this;
    }

    public int getProgress() {
        return progress;
    }

    public ActiveSpell setProgress(int progress) {
        this.progress = progress;
        return this;
    }

    public SpellCycle getCycle() {
        return cycle;
    }

    public ActiveSpell setCycle(SpellCycle cycle) {
        this.cycle = cycle;
        return this;
    }
}
