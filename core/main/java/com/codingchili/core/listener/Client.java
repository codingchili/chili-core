package com.codingchili.core.listener;

import io.vertx.core.Future;

import java.util.*;
import java.util.function.Function;

/**
 * @author Robin Duda
 *         <p>
 *         Represents the stateful connection over which requests may be passed.
 *         For stateless requests such as ClusterRequest and RestRequest the
 *         client may only live during a single request.
 */
public class Client implements Messageable {
    private static final String ID = "id";
    private Map<String, String> properties = new HashMap<>();
    private boolean authenticated = false;
    private Function<Object, Future<Void>> writer;

    public Client(Function<Object, Future<Void>> writer, String id) {
        this.writer = writer;
        properties.put(ID, id);
    }

    @Override
    public void write(Object object) {
        // todo rewrite Messageable to use Future?
        writer.apply(object);
    }

    public String id() {
        return properties.get(ID);
    }

    public Optional<String> getProperty(String key) {
        return Optional.ofNullable(properties.getOrDefault(key, null));
    }

    public void setProperty(String key, String value) {
        properties.put(key, value);
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
}
