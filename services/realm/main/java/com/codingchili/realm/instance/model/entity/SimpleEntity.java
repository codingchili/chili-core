package com.codingchili.realm.instance.model.entity;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.events.Event;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Set;
import java.util.UUID;

/**
 * @author Robin Duda
 */
public abstract class SimpleEntity implements Entity {
    protected String id = UUID.randomUUID().toString();
    protected String name = "<no name>";
    protected EventProtocol protocol = new EventProtocol(this);
    protected GameContext context;
    protected Vector vector = new Vector()
            .setX((float) (Math.random() * 1000))
            .setY((float) (Math.random() * 1000));

    @Override
    public void setContext(GameContext context) {
        context.subscribe(this);
    }

    @Override
    public void handle(Event event) {
        protocol.get(event.getType().toString()).submit(event);
    }

    @Override
    public Vector getVector() {
        return vector;
    }

    public void setVector(Vector vector) {
        this.vector = vector;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonIgnore
    @Override
    public Set<String> getInteractions() {
        return protocol.available();
    }
}
