package com.codingchili.core.listener;

import com.codingchili.core.protocol.Serializer;

import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;

import java.util.Map;
import java.util.UUID;

/**
 * A clustered session that can be used to write directly to a listener at the
 * edge of the cluster.
 */
class ClusteredSession implements Session {
    private JsonObject data = new JsonObject();
    private String id = UUID.randomUUID().toString();
    private String home;
    private SessionFactory<ClusteredSession> sessionFactory;

    public ClusteredSession() {
    }

    public ClusteredSession(SessionFactory<ClusteredSession> factory, String home, String id) {
        this.sessionFactory = factory;
        this.home = home;
        this.id = id;
        this.update();
    }

    @Override
    public Future<Boolean> isActive() {
        return sessionFactory.isActive(this);
    }

    @Override
    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    @Override
    public Future<Void> destroy() {
        return sessionFactory.destroy(this);
    }

    @Override
    public JsonObject asJson() {
        return data
                .put(Session.ID, id)
                .put(Session.HOME, home);
    }

    @Override
    public Future<Void> update() {
        return sessionFactory.update(this);
    }

    @Override
    public void write(Object object) {
        DeliveryOptions delivery = new DeliveryOptions()
                .addHeader(Session.ID, id)
                .addHeader(Session.HOME, home);

        sessionFactory.context().bus().send(home, Serializer.json(object), delivery);
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter to support serialization.
     *
     * @return json object as map.
     */
    public Map<String, Object> getData() {
        return data.getMap();
    }

    /**
     * setter to support serialization.
     *
     * @param data the data to set for the session.
     */
    public void setData(Map<String, Object> data) {
        this.data = new JsonObject(data);
    }

    public void setSessionFactory(SessionFactory<ClusteredSession> factory) {
        this.sessionFactory = factory;
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return compareTo(obj) == 0;
    }
}
