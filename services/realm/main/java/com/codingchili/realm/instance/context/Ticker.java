package com.codingchili.realm.instance.context;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author Robin Duda
 */
public class Ticker {
    private int id = UUID.randomUUID().hashCode();
    private GameContext context;
    private AtomicInteger tick;
    private Consumer<Ticker> exec;

    public Ticker(GameContext context, Consumer<Ticker> exec, Integer tick) {
        this.exec = exec;
        this.context = context;
        this.tick = new AtomicInteger(tick);
        context.setTicker(this);
    }

    public void run() {
        exec.accept(this);
    }

    public Ticker interval(Integer tick) {
        this.tick.set(tick);
        context.setTicker(this);
        return this;
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

    public Integer getTick() {
        return tick.get();
    }
}
