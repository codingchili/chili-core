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
 */
public class GameContext {
    public static final int TICK_RATE = 20;
    private Map<EventType, Map<Integer, EventProtocol>> listeners = new ConcurrentHashMap<>();
    private Map<String, Entity> entities = new ConcurrentHashMap<>();
    private Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private Set<Ticker> tickers = new ConcurrentHashSet<>();
    private SpellEngine spells = new SpellEngine(this);
    private AtomicBoolean processing = new AtomicBoolean(false);
    private AtomicBoolean closed = new AtomicBoolean(false);
    private InstanceContext instance;
    private Logger logger;

    private Long currentTick = 0L;
    private Grid grid;

    public GameContext(InstanceContext instance) {
        this.logger = instance.logger(getClass());
        this.instance = instance;
        this.grid = new Grid(256, instance.settings().getWidth());

        instance.periodic(() -> TICK_RATE, instance.address(), this::tick);
    }

    private AtomicInteger skippedTicks = new AtomicInteger(0);

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

                grid.update(entities.values());

                tickers.forEach(ticker -> {
                    if (currentTick % ticker.getTick() == 0) {
                        ticker.run();
                    }
                });

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

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

    public void addEntity(Entity entity) {
        entities.put(entity.getId(), entity);
        publish(new SpawnEvent().setEntity(entity));
        System.out.println("spawned entity " + entity.getId() + " at " + entity.getVector());
    }

    public void removeEntity(Entity entity) {
        entities.remove(entity.getId());
        publish(new SpawnEvent().setEntity(entity).setType(DESPAWN));
        unsubscribe(entity);
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
        Map<Integer, EventProtocol> scoped = listeners.computeIfAbsent(event.getType(), (key) -> new ConcurrentHashMap<>());
        String type = event.getType().toString();

        switch (event.getBroadcast()) {
            case PARTITION:
                // todo implement network partitions.
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

    public Collection<Entity> getEntities() {
        return entities.values();
    }

    public Optional<Entity> getEntity(Integer id) {
        return Optional.ofNullable(entities.get(id));
    }

    public static void main(String[] args) throws InterruptedException {
        RealmSettings settings = new RealmSettings().setName("testing");
        RealmContext realm = new RealmContext(new SystemContext(), settings);
        InstanceContext ins = new InstanceContext(realm, new InstanceSettings());

        GameContext game = new GameContext(ins);

        for (int i = 0; i < 200; i++) {
            game.addEntity(new TalkingPerson(game));
            game.addEntity(new TalkingPerson(game));
            game.addEntity(new ListeningPerson(game));
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

/*        game.addEntity(new TalkingPerson(game));
        game.addEntity(new TalkingPerson(game));
        game.addEntity(new ListeningPerson(game));*/
    }
}