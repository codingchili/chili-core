package com.codingchili.realm.instance.model.npc;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.*;
import com.codingchili.realm.instance.model.events.ChatEvent;
import com.codingchili.realm.instance.model.events.Event;

import java.util.UUID;

/**
 * @author Robin Duda
 */
public class TalkingPerson implements Entity {
    private Integer id = UUID.randomUUID().hashCode();
    private GameContext game;

    public TalkingPerson(GameContext game) {
        this.game = game;
        game.ticker(this::tick, 80);
    }

    public void tick(Ticker ticker) {
        Event event = new ChatEvent(this, "HELLO GUYS!");

        Grid grid = game.getGrid();

        game.getEntities().forEach(entity -> entity.notify(event));
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void notify(Event event) {
        // empty.
    }

    @Override
    public Vector getVector() {
        return new Vector().setX((float) (Math.random() * 600)).setY((float) (Math.random() * 600));
    }
}
