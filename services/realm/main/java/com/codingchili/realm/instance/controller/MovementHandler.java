package com.codingchili.realm.instance.controller;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.spells.SpellEngine;

import com.codingchili.core.listener.*;

/**
 * @author Robin Duda
 */
public class MovementHandler implements Receiver<Request> {
    private GameContext game;
    private SpellEngine spells;

    public MovementHandler(GameContext game) {
        this.game = game;
    }

    // todo: update vectors + handle collision/paths.
    // todo: cancel spells if casting non mobile spell.

  /*  public void move(MovementRequest request) {
        // todo: some system to update the vectors: should publish event.

        game.getById(request.sender()).setVector(request.getVector());
        // todo: call vector.update for each creature somewhere.
        game.publish("new movement event.", request.asEvent() ?);
    }*/

    @Override
    public void handle(Request request) {
        // todo handle player input
    }

    // todo write to client: easy just near the game context.
}
