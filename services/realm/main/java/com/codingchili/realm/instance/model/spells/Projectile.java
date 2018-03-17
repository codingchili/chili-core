package com.codingchili.realm.instance.model.spells;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.entity.Creature;
import com.codingchili.realm.instance.model.entity.Vector;
import com.codingchili.realm.instance.scripting.Bindings;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Robin Duda
 * <p>
 * A spell collider in the form of a projectile.
 */
public class Projectile {
    private static final String SOURCE = "source";
    private static final String TARGET = "target";
    private static final String SPELLS = "spells";
    private AtomicBoolean hit = new AtomicBoolean(false);
    private GameContext game;
    private Vector vector;
    private ActiveSpell spell;

    public Projectile(GameContext game, ActiveSpell spell) {
        // todo: read direction from SpellTarget
        this.game = game;
        this.spell = spell;
        this.vector = spell.getSource().getVector()
                .copy()
                .setDirection(0)
                .setVelocity(4.0f)
                .setSize(16);
    }

    public Projectile setDirection(float direction) {
        vector.setDirection(direction);
        return this;
    }

    public Projectile setVelocity(float velocity) {
        vector.setVelocity(velocity);
        return this;
    }

    public Projectile setSize(int size) {
        vector.setSize(size);
        return this;
    }

    // update position, check collisions.
    public boolean tick() {
        vector.forward();

        game.creatures().radius(vector).forEach(creature -> {
            spell.getSpell().onSpellActive.apply(getBindings(creature));
            hit.set(true);
        });

        return (hit.get() || (game.entities().radius(vector).size() > 0));
    }

    private Bindings getBindings(Creature target) {
        Bindings bindings = new Bindings();
        bindings.put(SOURCE, spell.getSource());
        bindings.put(TARGET, target);
        bindings.put(SPELLS, game.spells());
        return bindings;
    }
}
