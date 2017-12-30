package com.codingchili.realm.instance.model.entity;


import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.events.Event;

import java.util.Set;

import com.codingchili.core.protocol.Api;

/**
 * @author Robin Duda
 * <p>
 * model for portals used to travel between maps.
 */
public class Portal implements Entity {
    private EventProtocol protocol = new EventProtocol(this);
    private String name;
    private Vector vector;
    private Vector endpoint;
    private GameContext context;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getId() {
        return name;
    }

    @Override
    public Vector getVector() {
        return vector;
    }

    @Override
    public Set<String> getInteractions() {
        return protocol.available();
    }

    @Override
    public void setContext(GameContext context) {
        this.context = context;
    }

    @Api
    public void use(Event event) {
        event.getSource().ifPresent(source -> {
            context.getGrid().adjacent(vector).forEach(entity -> {
                if (entity.getId().equals(source.getId())) {
                    // todo perform preflight check, adjacency not in combat etc.
                    // todo figure out how to move an entity through realms.
                    // todo: we have a reference to the realm context, can we
                    // use the realm context to message upstreams?
                }
            });
        });
    }

    @Override
    public void handle(Event request) {
        protocol.get(request.getType().name()).submit(request);
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setVector(Vector vector) {
        this.vector = vector;
    }

    public Vector getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Vector endpoint) {
        this.endpoint = endpoint;
    }
}
