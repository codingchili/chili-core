package com.codingchili.realm.instance.context;

import com.codingchili.realm.configuration.RealmContext;
import com.codingchili.realm.instance.model.*;
import com.codingchili.realm.instance.model.npc.ListeningPerson;
import com.codingchili.realm.instance.model.npc.TalkingPerson;
import io.vertx.core.impl.ConcurrentHashSet;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.codingchili.core.context.SystemContext;

/**
 * @author Robin Duda
 */
public class GameContext {
    private boolean closed = false;
    private Map<Integer, Entity> entities = new ConcurrentHashMap<>();
    private Set<Ticker> tickers = new ConcurrentHashSet<>();
    private InstanceContext instance;
    private Long currentTick = 0L;
    private Grid grid;

    public GameContext(InstanceContext instance) {
        this.instance = instance;
        this.grid = new Grid(256, instance.settings().getWidth());

        instance.periodic(() -> 20, instance.address(), this::tick);
    }

    private void tick(Long timer) {
        instance.blocking(block -> {

            grid.update(entities.values());

            tickers.forEach(ticker -> {
                if (currentTick % ticker.getTick() == 0) {
                    ticker.run();
                }
            });
            block.complete();
        }, (done) -> {
            if (closed) {
                instance.cancel(timer);
                // todo: notify entities.
            }
            currentTick++;
            if (currentTick == Long.MAX_VALUE) {
                currentTick = 0L;
            }
        });
    }

    public Grid getGrid() {
        return grid;
    }

    public void close() {
        closed = true;
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
        System.out.println("spawned entity " + entity.getId() + " at " + entity.getVector());
        entities.put(entity.getId(), entity);
    }

    public void removeEntity(Entity entity) {
        entities.remove(entity.getId());
    }

    public Collection<Entity> getEntities() {
        return entities.values();
    }

    public Optional<Entity> getEntity(Integer id) {
        return Optional.ofNullable(entities.get(id));
    }

    public static void main(String[] args) {
        InstanceContext ins = new InstanceContext(new RealmContext(new SystemContext()), new InstanceSettings());
        GameContext game = new GameContext(ins);

        game.addEntity(new TalkingPerson(game));
        game.addEntity(new TalkingPerson(game));
        game.addEntity(new ListeningPerson());
    }
}