package com.codingchili.realm.instance.model.entity;


import com.codingchili.realm.instance.model.events.Event;

import com.codingchili.core.protocol.Api;

/**
 * @author Robin Duda
 * <p>
 * model for portals used to travel between maps.
 */
public class Portal extends SimpleEntity {
    private Vector endpoint;

    @Api
    public void use(Event event) {
        if (event.getSource() != null) {
            context.creatures().adjacent(vector).forEach(entity -> {
                if (entity.getId().equals(event.getSource())) {
                    // todo perform preflight check, adjacency not in combat etc.
                    // todo figure out how to move an entity through realms.
                    // todo: we have a reference to the realm context, can we
                    // use the realm context to message upstreams?
                }
            });
        }
    }

    public Vector getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Vector endpoint) {
        this.endpoint = endpoint;
    }
}
