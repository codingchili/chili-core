package com.codingchili.realm.instance.controller;

import com.codingchili.realm.instance.context.GameContext;

import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;

/**
 * @author Robin Duda
 */
public class MovementHandler implements CoreHandler<Request> {
    private GameContext game;

    public MovementHandler(GameContext game) {
        this.game = game;
    }

    @Override
    public void handle(Request request) {
        // todo handle player input
    }

    // todo write to client: easy just near the game context.
}
