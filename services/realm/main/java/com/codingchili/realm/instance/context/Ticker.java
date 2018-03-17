package com.codingchili.realm.instance.context;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Robin Duda
 *
 * Ticker executes periodically on the game loop.
 */
public class Ticker implements Supplier<Integer> {
    private int id = UUID.randomUUID().hashCode();
    private GameContext context;
    private AtomicInteger tick;
    private Long lastTick = System.currentTimeMillis();
    private Consumer<Ticker> exec;

    public Ticker(GameContext context, Consumer<Ticker> exec, Integer tick) {
        this.exec = exec;
        this.context = context;
        this.tick = new AtomicInteger(tick);
        context.setTicker(this);
    }

    public void run() {
        Long currentTick = System.currentTimeMillis();

        if (lastTick > currentTick) {
            // prevent overflow caused by low accuracy.
            lastTick = currentTick;
        } else {
            lastTick = currentTick - lastTick;
        }
        exec.accept(this);
        lastTick = System.currentTimeMillis();
    }

    public Ticker interval(Integer tick) {
        this.tick.set(tick);
        context.setTicker(this);
        return this;
    }

    public Long delta() {
        return lastTick;
    }

    public Ticker disable() {
        tick.set(0);
        context.setTicker(this);
        return this;
    }

    @Override
    public boolean equals(Object other) {
        return ((Ticker) other).id == id;
    }

    @Override
    public int hashCode() {
        return id;
    }

        @Override
    public Integer get() {
        return tick.get();
    }
}
