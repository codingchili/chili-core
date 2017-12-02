package com.codingchili.realm.instance.model;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.events.Event;

import java.util.UUID;

/**
 * @author Robin Duda
 */
public abstract class SimpleEntity implements Entity {
    protected Integer id = UUID.randomUUID().hashCode();
    protected EventProtocol<Event> protocol;
    protected Vector vector = new Vector()
            .setX((float) (Math.random() * 1000))
            .setY((float) (Math.random() * 1000));

    public SimpleEntity(GameContext context) {
        protocol = context.subscribe(this);
    }

    @Override
    public void handle(Event event) {
        protocol.get(event.getType().toString()).submit(event);
    }

    @Override
    public Vector getVector() {
        return vector;
    }

    @Override
    public Integer getId() {
        return id;
    }
}
