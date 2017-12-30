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

import com.codingchili.core.context.SystemContext;
import com.codingchili.core.logging.Logger;

import static com.codingchili.realm.instance.model.events.SpawnEvent.SpawnType.DESPAWN;

/**
 * @author Robin Duda
 *
 * The core game loop.
 */
public class GameContext {
    public static final int TICK_INTERVAL_MS = 20;
    private Map<EventType, Map<String, EventProtocol>> listeners = new ConcurrentHashMap<>();
    private Map<String, Creature> creatures = new ConcurrentHashMap<>();
    private Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private Set<Ticker> tickers = new ConcurrentHashSet<>();
    private SpellEngine spells = new SpellEngine(this);
    private AtomicInteger skippedTicks = new AtomicInteger(0);
    private AtomicBoolean processing = new AtomicBoolean(false);
    private AtomicBoolean closed = new AtomicBoolean(false);
    private InstanceContext instance;
    private Logger logger;

    private Long currentTick = 0L;
    private Grid grid;

    public GameContext(InstanceContext instance) {
        this.logger = instance.logger(getClass());
        this.instance = instance;

        // the grid update is the first in each tick.
        this.grid = new Grid(this, 256);

        instance.periodic(() -> TICK_INTERVAL_MS, instance.address(), this::tick);
    }

    // the game loop.
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
                if (closed.get()) {
                    instance.cancel(timer);
                } else {
                    currentTick++;
                    if (currentTick == Long.MAX_VALUE) {
                        currentTick = 0L;
                    }
                }
                processing.set(false);
            });
        }
    }

    public GameContext runLater(Runnable runnable) {
        queue.add(runnable);
        return this;
    }

    public Grid getGrid() {
        return grid;
    }

    public SpellEngine getSpells() {
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

    public void addCreature(Creature creature) {
        creature.setContext(this);
        creatures.put(creature.getId(), creature);
        publish(new SpawnEvent().setCreature(creature));
        System.out.println("spawned entity " + creature.getId() + " at " + creature.getVector());
    }

    public void removeEntity(Creature creature) {
        creatures.remove(creature.getId());
        publish(new SpawnEvent().setCreature(creature).setType(DESPAWN));
        unsubscribe(creature);
    }

    public void unsubscribe(Entity entity) {
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
                    grid.adjacent(source.getVector()).forEach(entity -> {
                        scoped.get(entity.getId()).get(type).submit(event);
                    });
                });
                break;
        }
    }

    public Logger getLogger() {
        return instance.logger(getClass());
    }

    public Collection<Creature> getCreatures() {
        return creatures.values();
    }

    public Optional<Creature> getCreature(String id) {
        return Optional.ofNullable(creatures.get(id));
    }

    public InstanceContext getInstance() {
        return instance;
    }

    public static void main(String[] args) throws InterruptedException {
        RealmSettings settings = new RealmSettings().setName("testing");
        RealmContext realm = new RealmContext(new SystemContext(), settings);
        InstanceContext ins = new InstanceContext(realm, new InstanceSettings());

        GameContext game = new GameContext(ins);

        for (int i = 0; i < 200; i++) {
            game.addCreature(new TalkingPerson());
            game.addCreature(new TalkingPerson());
            game.addCreature(new ListeningPerson());
        }

        System.out.println("BEGIN");
        long time = System.currentTimeMillis();
       /* for (int i = 0; i < 100000; i++) {
            game.tick(0L);

            if (i % 100 == 0)
                System.out.println(i);
        }*/
        System.out.println("END: " + (System.currentTimeMillis() - time) + "ms.");
        System.out.println(ListeningPerson.called);

        //game.ticker((ticker) -> System.out.println(ticker.delta()), 1);

        //  System.exit(0);

/*        game.addCreature(new TalkingPerson(game));
        game.addCreature(new TalkingPerson(game));
        game.addCreature(new ListeningPerson(game));*/
    }
}