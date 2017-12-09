package com.codingchili.realm.instance.model.npc;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.*;
import com.codingchili.realm.instance.model.events.ChatEvent;
import com.codingchili.realm.instance.model.events.Event;

/**
 * @author Robin Duda
 */
public class TalkingPerson extends SimpleEntity {
    private GameContext game;

    public TalkingPerson(GameContext game) {
        super(game);
        this.game = game;
        game.ticker(this::tick, 80);
    }

    public void tick(Ticker ticker) {
        Event event = new ChatEvent(this, "HELLO GUYS!" + id);
 //       System.out.println("published EVENT");
        game.publishEvent(event);
    }
}
