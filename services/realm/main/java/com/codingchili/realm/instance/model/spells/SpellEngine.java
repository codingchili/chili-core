package com.codingchili.realm.instance.model.spells;

import com.codingchili.realm.configuration.RealmContext;
import com.codingchili.realm.configuration.RealmSettings;
import com.codingchili.realm.instance.context.*;
import com.codingchili.realm.instance.model.afflictions.ActiveAffliction;
import com.codingchili.realm.instance.model.afflictions.Affliction;
import com.codingchili.realm.instance.model.entity.Entity;
import com.codingchili.realm.instance.model.entity.SimpleEntity;
import com.codingchili.realm.instance.model.npc.ListeningPerson;
import com.codingchili.realm.instance.model.stats.Attribute;
import io.vertx.core.json.JsonObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.codingchili.core.context.SystemContext;
import com.codingchili.core.files.ConfigurationFactory;
import com.codingchili.core.protocol.Serializer;

// todo: is the affliction list shared between contexts? players can move between contexts.
public class SpellEngine {
    // todo: add a spellbase: spellbase should be shared to save memory.
    private Map<Entity, Spell> casting = new ConcurrentHashMap<>();
    private GameContext game;
    private Long tick = 0L;

    public SpellEngine(GameContext game) {
        this.game = game;

        // ticker can run slow as it only affects active afflictions.
        game.ticker(this::tick, 1);
    }

    public boolean cast(Entity caster, Spell spell) {
        // todo: check if entity knows the spell in the spellbook.
        // todo: cast spell with casttime
        // todo: emit event
        // todo: check if the entity is allowed to cast the spell
        // todo: cancel spell if already is casting
        return true;
    }

    private void casted() {
        // todo: emit event.
        // todo: remove casting event
        // todo: apply cooldown.
        // todo: apply gcd
    }

    public void cancel() {

        // todo: apply gcd
        // todo: emit event
    }

    public void afflict(Entity source, Entity target, Affliction affliction) {
        //System.out.println("Afflicted !");
        //System.out.println("Afflicted entity has str = " + target.getBaseStats().get(Attribute.strength));

        source.getAfflictions().add(affliction.apply(source, target), game);
        // todo: emit event.
        // todo: add affliction
    }

    public void damage(ActiveAffliction active, double value) {
        damage(active.getSource(), active.getTarget(), value, active.getAffliction().getType());
    }

    public void damage(Entity source, Entity target, double value, DamageType type) {
        //System.out.println("damaging entity " + target.getName() + " for " + value + " of type " + type.name());

        target.getBaseStats().add(Attribute.health, (int) value);
       /* System.out.println("current health " + target.getStats().get(Attribute.health));
        System.out.println("current strength " + target.getStats().get(Attribute.strength));
        System.out.println("current strength " + source.getStats().get(Attribute.strength));*/
        // todo: damage entity
        // todo: check if dead
        // todo: damage type is enum?
    }

    private void tick(Ticker ticker) {
        game.getEntities().forEach(entity -> {
            entity.getAfflictions().removeIf(affliction -> {
                if ((affliction.getStart() + tick) % affliction.getInterval() == 0) {
                    return !(affliction.tick(game));
                }
                return false;
            }, game);
        });

        // todo: remove states where the lists are empty.
        // todo: tick: perform spell ticks, countdown casttime etc.
        // todo: check spell cooldowns and global cooldowns.

        tick++;
        if (tick == Long.MAX_VALUE) {
            tick = 0L;
        }
    }

    public static void main(String[] args) {
        RealmSettings settings = new RealmSettings().setName("testing");
        RealmContext realm = new RealmContext(new SystemContext(), settings);
        InstanceContext ins = new InstanceContext(realm, new InstanceSettings());
        GameContext game = new GameContext(ins);

        SpellEngine engine = new SpellEngine(game);

        JsonObject affConfig = ConfigurationFactory.readObject("/afflictiontest.yaml");
        Affliction affliction = Serializer.unpack(affConfig, Affliction.class);

        SimpleEntity target = new ListeningPerson(game);
        SimpleEntity source = new ListeningPerson(game);

        game.addEntity(target);
        game.addEntity(source);

        for (int i = 0; i < 500; i ++) {
            engine.afflict(source, target, affliction);
        }
    }
}
