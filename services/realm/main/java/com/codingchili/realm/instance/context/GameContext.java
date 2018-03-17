package com.codingchili.realm.instance.context;

import com.codingchili.realm.configuration.RealmContext;
import com.codingchili.realm.configuration.RealmSettings;
import com.codingchili.realm.instance.model.entity.*;
import com.codingchili.realm.instance.model.events.*;
import com.codingchili.realm.instance.model.npc.ListeningPerson;
import com.codingchili.realm.instance.model.spells.SpellEngine;
import com.codingchili.realm.instance.model.spells.SpellTarget;
import io.vertx.core.impl.ConcurrentHashSet;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.codingchili.core.context.SystemContext;
import com.codingchili.core.logging.Logger;

import static com.codingchili.realm.instance.model.events.SpawnEvent.SpawnType.DESPAWN;

/**
 * @author Robin Duda
 * <p>
 * The core game loop.
 */
public class GameContext {
    private static final int TICK_INTERVAL_MS = 20;
    private Map<EventType, Map<String, EventProtocol>> listeners = new ConcurrentHashMap<>();
    private Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private Set<Ticker> tickers = new ConcurrentHashSet<>();
    private AtomicInteger skippedTicks = new AtomicInteger(0);
    private AtomicBoolean processing = new AtomicBoolean(false);
    private AtomicBoolean closed = new AtomicBoolean(false);
    private InstanceContext instance;
    private Grid<Creature> creatures;
    private Grid<Entity> structures;
    private SpellEngine spells;
    private Long currentTick = 0L;

    public GameContext(InstanceContext instance) {
        this.instance = instance;

        int width = instance.settings().getWidth();
        this.creatures = new Grid<>(width);
        this.structures = new Grid<>(width);

        ticker(creatures::update, 1);
        ticker(structures::update, 5);

        spells = new SpellEngine(this);

        instance.periodic(() -> TICK_INTERVAL_MS, instance.address(), this::tick);
    }

    private void tick(Long timer) {
        if (processing.getAndSet(true)) {
            skippedTicks.incrementAndGet();
        } else {

            if (skippedTicks.get() > 0) {
                instance.skippedTicks(skippedTicks.getAndSet(0));
            }

            instance.blocking(block -> {
                Runnable runnable;

                while ((runnable = queue.poll()) != null) {
                    runnable.run();
                }

                tickers.forEach(ticker -> {
                    if (currentTick % ticker.get() == 0) {
                        ticker.run();
                    }
                });

                block.complete();
            }, (done) -> {
                if (done.succeeded()) {
                    if (closed.get()) {
                        instance.cancel(timer);
                    } else {
                        currentTick++;
                        if (currentTick == Long.MAX_VALUE) {
                            currentTick = 0L;
                        }
                    }
                } else {
                    done.cause().printStackTrace();
                }
                processing.set(false);
            });
        }
    }

    public GameContext queue(Runnable runnable) {
        queue.add(runnable);
        return this;
    }

    public Grid<Creature> creatures() {
        return creatures;
    }

    public Grid<Entity> entities() {
        return structures;
    }

    public SpellEngine spells() {
        return spells;
    }

    public void close() {
        closed.set(true);
        publish(new ShutdownEvent());
    }

    public Ticker ticker(Consumer<Ticker> runnable, Integer interval) {
        return new Ticker(this, runnable, interval);
    }

    public void setTicker(Ticker ticker) {
        if (ticker.get() > 0) {
            tickers.add(ticker);
        } else {
            tickers.remove(ticker);
        }
    }

    public void add(Creature creature) {
        creatures.add(creature);
        addNew(creature);
    }

    public void add(Entity entity) {
        structures.add(entity);
        addNew(entity);
    }

    private void addNew(Entity entity) {
        entity.setContext(this);
        publish(new SpawnEvent().setEntity(entity));
        subscribe(entity);
    }

    public void remove(Entity entity) {
        creatures.remove(entity.getId());
        structures.remove(entity.getId());
        publish(new SpawnEvent().setEntity(entity).setType(DESPAWN));
        unsubscribe(entity);
    }

    private void unsubscribe(Entity entity) {
        listeners.forEach((key, value) -> value.remove(entity.getId()));
    }

    public EventProtocol subscribe(Entity entity) {
        EventProtocol protocol = new EventProtocol(entity);

        protocol.available().stream()
                .map(EventType::valueOf)
                .forEach(event -> {
                    listeners.computeIfAbsent(event, (key) -> new ConcurrentHashMap<>());
                    listeners.get(event).put(protocol.getId(), protocol);
                });

        return protocol;
    }

    // publish-subscribe does not support unicast.
    public void publish(Event event) {
        Map<String, EventProtocol> scoped = listeners.computeIfAbsent(event.getType(), (key) -> new ConcurrentHashMap<>());
        String type = event.getType().toString();

        //System.out.println(Serializer.yaml(event));

        switch (event.getBroadcast()) {
            case PARTITION:
                // todo implement network partitioning.
            case GLOBAL:
                scoped.values().forEach(listener -> listener.get(type).submit(event));
                break;
            case ADJACENT:
                Stream.of(creatures, structures).forEach(grid -> {
                    grid.adjacent(getById(event.getSource()).getVector()).forEach(entity -> {
                        scoped.get(entity.getId()).get(type).submit(event);
                    });
                });
                break;
        }
    }

    public Entity getById(String id) {
        Entity entity = null;
        if (creatures.exists(id)) {
            entity = creatures.get(id);
        } else {
            if (structures.exists(id)) {
                entity = structures.get(id);
            }
        }
        Objects.requireNonNull(entity, String.format("Could not find entity with id '%s'.", id));
        return entity;
    }

    public Logger getLogger(Class<?> aClass) {
        return instance.logger(aClass);
    }

    public InstanceContext getInstance() {
        return instance;
    }

    public static Integer secondsToTicks(double seconds) {
        return (int) (seconds * 1000 / TICK_INTERVAL_MS);
    }

    public static double ticksToSeconds(int ticks) {
        return (ticks * TICK_INTERVAL_MS) / 1000;
    }

    public static void main(String[] args) throws InterruptedException {
        RealmSettings settings = new RealmSettings().setName("testing");

        RealmContext.create(new SystemContext(), settings).setHandler(create -> {
            RealmContext realm = create.result();


            InstanceContext ins = new InstanceContext(realm, new InstanceSettings());

            GameContext game = new GameContext(ins);

/*        game.ticker(ticker -> {
            System.out.println("DING");
        }, 50);*/

            Creature cl = new ListeningPerson();
            game.add(cl);

            // cast the poison spell on himself.
            cl.getSpells().getLearned().add("poisoner");

            for (int i = 0; i < 2; i++) {
                boolean casted = game.spells.cast(cl, new SpellTarget().setCreature(cl), "poisoner");
                System.out.println("casted = " + casted);
            }
        });


        // todo: add more hooks to SpellEngine afflictions. onDamage etc.
        // todo: test cases
        // todo: script npcs: onDeath, onAI etc.
        // - afflict, duration, cancel etc.
        // - cast spell: cooldown, charges etc.

        /*for (int i = 0; i < 200; i++) {
            game.add(new TalkingPerson());
            game.add(new TalkingPerson());
            game.add(new ListeningPerson());
        }*/

        /*game.ticker(ticker -> {
            System.out.println(ListeningPerson.called);
        }, TICK_INTERVAL_MS);*/

        //game.ticker((ticker) -> System.out.println(ticker.delta()), 1);

        //  System.exit(0);

/*        game.addCreature(new TalkingPerson(game));
        game.addCreature(new TalkingPerson(game));
        game.addCreature(new ListeningPerson(game));*/
    }

    /*public static void main(String[] args) {
       System.out.println(secondsToTicks(0.5));
    }*/
}