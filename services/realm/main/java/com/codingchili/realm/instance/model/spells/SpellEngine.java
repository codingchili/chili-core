package com.codingchili.realm.instance.model.spells;

import com.codingchili.realm.configuration.RealmContext;
import com.codingchili.realm.configuration.RealmSettings;
import com.codingchili.realm.instance.context.*;
import com.codingchili.realm.instance.model.afflictions.*;
import com.codingchili.realm.instance.model.entity.*;
import com.codingchili.realm.instance.model.events.*;
import com.codingchili.realm.instance.model.npc.ListeningPerson;
import com.codingchili.realm.instance.model.stats.Attribute;
import io.vertx.core.json.JsonObject;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.codingchili.core.context.SystemContext;
import com.codingchili.core.files.ConfigurationFactory;
import com.codingchili.core.protocol.Serializer;

/**
 * Manages spell casting, afflictions and damaging.
 */
public class SpellEngine {
    private Map<Creature, ActiveSpell> casting = new ConcurrentHashMap<>();
    private Collection<ActiveSpell> passive = new ConcurrentLinkedQueue<>();
    private Collection<Projectile> projectiles = new ConcurrentLinkedQueue<>();
    private Grid<Creature> creatures;
    private AfflictionDB afflictions;
    private SpellDB spells;
    private GameContext game;
    private Integer tick = 0;

    public SpellEngine(GameContext game) {
        this.game = game;
        this.creatures = game.creatures();
        this.spells = new SpellDB(game);
        this.afflictions = new AfflictionDB(game);

        game.ticker(this::tick, 1);
    }

    public boolean cast(Creature caster, SpellTarget target, String spellName) {
        Spell spell = spells.getByName(spellName);

        if (caster.getSpells().learned(spellName)) {
            if (caster.getSpells().cooldown(spell)) {
                return false;
            } else {
                ActiveSpell active = new ActiveSpell(spell)
                        .setSource(caster)
                        .setTarget(target);

                if (active.onCastBegin(game)) {
                    cancel(caster);
                    casting.put(active.getSource(), active);
                    game.publish(new SpellCastEvent(active));
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    public void cancel(Creature caster) {
        ActiveSpell spell = casting.get(caster);
        if (spell != null) {
            game.publish(new SpellCastEvent(spell.setCycle(SpellCycle.CANCELLED)));
            casting.remove(caster);
        }
    }

    public void cleanse(Creature target, String affliction) {
        // todo: remove an active affliction or all.
    }

    public void afflict(Creature source, String affliction) {
        afflict(source, source, affliction);
    }

    // todo: call active afflictions on target with registered callback for onAfflict

    public void afflict(Creature source, Creature target, String name) {
        ActiveAffliction affliction = afflictions.getByName(name).apply(source, target);
        source.getAfflictions().add(affliction, game);
        game.publish(new AfflictionEvent(affliction));
    }

    // todo: call active afflictions on target with registered callback for onInterrupt

    public void interrupt(Creature caster) {
        ActiveSpell spell = casting.get(caster);
        if (spell != null) {
            game.publish(new SpellCastEvent(spell.setCycle(SpellCycle.INTERRUPTED)));
            casting.remove(caster);
        }
    }

    public void energy(Creature target, int amount) {
        target.getStats().update(Attribute.energy, amount);
    }

    // todo: call active afflictions on target with registered callback for onHeal.

    public void heal(Creature target, double value) {
        float max = target.getBaseStats().get(Attribute.maxhealth);
        float current = target.getBaseStats().get(Attribute.health);
        float next = (float) Math.min(max, current + value);

        target.getBaseStats().set(Attribute.health, next);

        game.publish(new DamageEvent(target, value, DamageType.heal));
    }

    public void projectile(ActiveSpell spell, Map<String, Float> properties) {
        projectiles.add(new Projectile(game, spell)); // todo set properties.
    }

    public Spell getSpellByName(String spellName) {
        return spells.getByName(spellName);
    }

    // todo: call active afflictions on target with registered callback for onDamage.

    public void damage(Creature source, Creature target, double value, DamageType type) {
        target.getBaseStats().update(Attribute.health, (int) value);

        game.publish(new DamageEvent(target, value, type).setSource(source));

        if (target.getStats().get(Attribute.health) < 0) {
            // todo: if the player is already dead: dont kill it AGAIN.
            // prevent healing/damaging dead targets, remove afflictions etc.v
            game.publish(new DeathEvent(target, source));
        }
    }

    private void tick(Ticker ticker) {
        updateCreatureSpellState();
        updateCastingProgress();
        updateActiveSpells();
        updateProjectiles();

        if (tick == Integer.MAX_VALUE) {
            tick = 0;
        }
        tick++;
    }

    // update affliction state and spell cooldowns.
    private void updateCreatureSpellState() {
        creatures.all().forEach(entity -> {
            entity.getAfflictions().removeIf(active -> {
                if (active.shouldTick(active.getStart() + tick))
                    if (!active.tick(game)) return true;
                return false;
            }, game);

            entity.getSpells().tick(spells, tick);
        });
    }

    // update progress for spells currently being casted.
    private void updateCastingProgress() {
        casting.values().removeIf((casting) -> {
            if (casting.completed()) {
                game.publish(new SpellCastEvent(casting.setCycle(SpellCycle.CASTED)));
                casting.onCastCompleted(game);

                // the spell is casted: stay active until the spell expires.
                passive.add(casting);
                return true;
            } else {
                if (casting.shouldTick(tick)) {
                    casting.onCastProgress(game, tick);
                }
            }
            return false;
        });
    }

    // execute spell effects for spells that have been casted successfully.
    private void updateActiveSpells() {
        passive.removeIf(spell -> {
            if (spell.active()) {

                if (spell.shouldTick(tick)) {
                    spell.onSpellEffects(game, tick);
                }

                return false;
            } else {
                return true;
            }
        });
    }

    private void updateProjectiles() {
        projectiles.removeIf(Projectile::tick);
    }

    public static void main(String[] args) {
        RealmSettings settings = new RealmSettings().setName("testing");

        RealmContext.create(new SystemContext(), settings).setHandler(create -> {
           RealmContext rc = create.result();


            InstanceContext ins = new InstanceContext(rc, new InstanceSettings());
            GameContext game = new GameContext(ins);

            SpellEngine engine = new SpellEngine(game);

            JsonObject affConfig = ConfigurationFactory.readObject("/afflictiontest.yaml");
            Affliction affliction = Serializer.unpack(affConfig, Affliction.class);

            Creature target = new ListeningPerson();
            Creature source = new ListeningPerson();

            game.add(target);
            game.add(source);

            for (int i = 0; i < 500; i++) {
                engine.afflict(source, target, affliction.getName());
            }
        });
    }
}
