package com.codingchili.realm.instance.context;

import com.codingchili.realm.configuration.RealmContext;
import com.codingchili.realm.configuration.RealmSettings;
import com.codingchili.realm.instance.model.entity.*;
import com.codingchili.realm.instance.model.events.*;
import com.codingchili.realm.instance.model.npc.ListeningPerson;
import com.codingchili.realm.instance.model.npc.TalkingPerson;
import com.codingchili.realm.instance.model.spells.SpellEngine;
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
    public static final int TICK_INTERVAL_MS = 20;
    private Map<EventType, Map<String, EventProtocol>> listeners = new ConcurrentHashMap<>();
    private Map<String, Creature> creatureMap = new ConcurrentHashMap<>();
    private Map<String, Entity> structureMap = new ConcurrentHashMap<>();
    private Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private Set<Ticker> tickers = new ConcurrentHashSet<>();
    private AtomicInteger skippedTicks = new AtomicInteger(0);
    private AtomicBoolean processing = new AtomicBoolean(false);
    private AtomicBoolean closed = new AtomicBoolean(false);
    private SpellEngine spells;
    private InstanceContext instance;

    private Long currentTick = 0L;
    private Grid<Creature> creatures;
    private Grid<Entity> entities;

    public GameContext(InstanceContext instance) {
        this.instance = instance;

        int width = instance.settings().getWidth();
        this.creatures = new Grid<>(width, creatureMap::values);
        this.entities = new Grid<>(width, structureMap::values);

        ticker(creatures::update, 1);
        ticker(entities::update, 5);

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
                    if (currentTick % ticker.getTick() == 0) {
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
        return entities;
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
        if (ticker.getTick() > 0) {
            tickers.add(ticker);
        } else {
            tickers.remove(ticker);
        }
    }

    public void add(Creature creature) {
        creatureMap.put(creature.getId(), creature);
        addNew(creature);
    }

    public void add(Entity entity) {
        structureMap.put(entity.getId(), entity);
        addNew(entity);
    }

    private void addNew(Entity entity) {
        entity.setContext(this);
        publish(new SpawnEvent().setEntity(entity));
        subscribe(entity);
    }

    public void remove(Entity entity) {
        creatureMap.remove(entity.getId());
        structureMap.remove(entity.getId());
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

    public void publish(Event event) {
        Map<String, EventProtocol> scoped = listeners.computeIfAbsent(event.getType(), (key) -> new ConcurrentHashMap<>());
        String type = event.getType().toString();

        switch (event.getBroadcast()) {
            case PARTITION:
            case GLOBAL:
                scoped.values().forEach(listener -> listener.get(type).submit(event));
                break;
            case ADJACENT:
                event.getSource().ifPresent(source -> {
                    Stream.of(creatures, entities).forEach(grid -> {
                        grid.adjacent(source.getVector()).forEach(entity -> {
                            scoped.get(entity.getId()).get(type).submit(event);
                        });
                    });
                });
                break;
        }
    }

    public Logger getLogger(Class<?> aClass) {
        return instance.logger(aClass);
    }

    public InstanceContext getInstance() {
        return instance;
    }

   /* public static void main(String[] args) throws InterruptedException {
        RealmSettings settings = new RealmSettings().setName("testing");
        RealmContext realm = new RealmContext(new SystemContext(), settings);
        InstanceContext ins = new InstanceContext(realm, new InstanceSettings());

        GameContext game = new GameContext(ins);

        for (int i = 0; i < 200; i++) {
            game.add(new TalkingPerson());
            game.add(new TalkingPerson());
            game.add(new ListeningPerson());
        }

        game.ticker(ticker -> {
            System.out.println(ListeningPerson.called);
        }, TICK_INTERVAL_MS);

        //game.ticker((ticker) -> System.out.println(ticker.delta()), 1);

        //  System.exit(0);

*//*        game.addCreature(new TalkingPerson(game));
        game.addCreature(new TalkingPerson(game));
        game.addCreature(new ListeningPerson(game));*//*
    }*/

    public static Integer secondsToTicks(double seconds) {
        return (int) (seconds * 1000 / TICK_INTERVAL_MS);
    }

    public static void main(String[] args) {
       System.out.println(secondsToTicks(0.5));
    }
}