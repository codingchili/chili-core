package com.codingchili.realm.instance.model;

import com.codingchili.realm.instance.context.GameContext;

/**
 * @author Robin Duda
 * model for portals used to travel between maps.
 */
public class Portal extends SimpleEntity {

    public Portal(GameContext context) {
        super(context);
    }

    @Override
    public Integer getId() {
        return null;
    }

    @Override
    public Vector getVector() {
        return null;
    }
}
