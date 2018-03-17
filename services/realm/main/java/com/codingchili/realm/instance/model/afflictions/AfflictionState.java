package com.codingchili.realm.instance.model.afflictions;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.stats.Stats;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/**
 * @author Robin Duda
 */
public class AfflictionState {
    private Stats stats = new Stats();
    private Queue<ActiveAffliction> list = new ConcurrentLinkedQueue<>();

    @JsonIgnore
    public Stats getStats() {
        return stats;
    }

    @JsonIgnore
    public void setStats(Stats modifiers) {
        this.stats = modifiers;
    }

    public Queue<ActiveAffliction> getList() {
        return list;
    }

    public void setList(Queue<ActiveAffliction> list) {
        this.list = list;
    }

    public void add(ActiveAffliction affliction, GameContext game) {
        list.add(affliction);
        update(game);
    }

    public boolean has(String afflictionName) {
        for (ActiveAffliction active : list) {
            if (active.getAffliction().getName().equals(afflictionName)) {
                return true;
            }
        }
        return false;
    }

    public void removeIf(Predicate<ActiveAffliction> predicate, GameContext game) {
        AtomicBoolean removed = new AtomicBoolean(false);

        list.removeIf((affliction) -> {
            if (predicate.test(affliction)) {
                removed.set(true);
                return true;
            }
            return false;
        });

        if (removed.get()) {
            update(game);
            //System.out.println("post remove str is " + stats.get(Attribute.strength));
        }
    }

    public void update(GameContext game) {
        stats.clear();
        list.forEach(active ->
                stats = stats.apply(active.modify(game)));

        //System.out.println("post update str is " + stats.get(Attribute.strength));
    }
}
