package com.codingchili.realm.instance.model.spells;

import com.codingchili.realm.configuration.RealmContext;
import com.codingchili.realm.instance.context.*;
import com.codingchili.realm.instance.model.entity.Entity;
import com.codingchili.realm.instance.model.entity.SimpleEntity;
import com.codingchili.realm.instance.model.npc.ListeningPerson;
import com.codingchili.realm.instance.model.stats.Attribute;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.context.SystemContext;
import com.codingchili.core.files.ConfigurationFactory;
import com.codingchili.core.protocol.Serializer;

// todo: is the affliction list shared between contexts? players can move between contexts.
public class SpellEngine {
    // todo: add a spellbase: spellbase should be shared to save memory.
    private GameContext game;

    public SpellEngine(GameContext game) {
        this.game = game;

        // ticker can run slow as it only affects active afflictions.
        game.ticker(this::tick, 5);
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
        // todo: emit event
        // todo: cancel spells (when moving etc)
        // todo: apply gcd
    }

    public void afflict(Entity source, Entity target, Affliction affliction) {
        System.out.println("Afflicted !");
        System.out.println("Afflicted entity has str = " + target.getBaseStats().get(Attribute.strength));

        source.getAfflictions().add(affliction.apply(source, target), game);
        // todo: emit event.
        // todo: add affliction
    }

    public void damage(Entity source, Entity target, double value, String type) {
        System.out.println("damaging entity " + target.getName() + " for " + value + " of type " + type);

        target.getBaseStats().add(Attribute.health, (int) value);
        System.out.println("current health " + target.getStats().get(Attribute.health));
        System.out.println("current strength " + target.getStats().get(Attribute.strength));
        System.out.println("current strength " + source.getStats().get(Attribute.strength));
        // todo: damage entity
        // todo: check if dead
        // todo: damage type is enum?
    }

    private void tick(Ticker ticker) {
        game.getEntities().forEach(entity -> {
            entity.getAfflictions().removeIf(affliction ->
                    !affliction.tick(game), game);
        });

        // todo: remove states where the lists are empty.
        // todo: tick: perform spell ticks, countdown casttime etc.
        // todo: check spell cooldowns and global cooldowns.
        // todo: process afflictions.
    }

    public static void main(String[] args) {
        InstanceContext ins = new InstanceContext(new RealmContext(new SystemContext()), new InstanceSettings());
        GameContext game = new GameContext(ins);

        SpellEngine engine = new SpellEngine(game);

        JsonObject affConfig = ConfigurationFactory.readObject("/afflictiontest.yaml");
        Affliction affliction = Serializer.unpack(affConfig, Affliction.class);

        SimpleEntity target = new ListeningPerson(game);
        SimpleEntity source = new ListeningPerson(game);

        game.addEntity(target);
        game.addEntity(source);

        engine.afflict(source, target, affliction);
    }
}
