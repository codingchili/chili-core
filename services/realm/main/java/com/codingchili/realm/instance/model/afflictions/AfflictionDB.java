package com.codingchili.realm.instance.model.afflictions;

import com.codingchili.realm.instance.context.GameContext;

/**
 * @author Robin Duda
 *
 * Container of all afflictions.
 */
public class AfflictionDB {
    private GameContext game;

    public AfflictionDB(GameContext game) {
        this.game = game;
    }

    public Affliction getByName(String affliction) {
        return null;
    }
}
