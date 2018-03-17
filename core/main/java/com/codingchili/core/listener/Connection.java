package com.codingchili.core.listener;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author Robin Duda
 * <p>
 * Represents the stateful connection over which requests may be passed.
 * For stateless requests such as ClusterRequest and RestRequest the
 * client may only live during a single request.
 */
public class Connection implements Messageable {
    private Map<String, String> properties = new HashMap<>();
    private List<Runnable> closeHandlers = new ArrayList<>();
    private static final String ID = "id";
    private Consumer<Object> writer;

    /**
     * Creates a new stateful connection that properly implements the ID method.
     *
     * @param writer a method that writes outbound messages to the remote peer.
     * @param id     the unique id of this connection.
     */
    public Connection(Consumer<Object> writer, String id) {
        this.writer = writer;
        properties.put(ID, id);
    }

    @Override
    public void write(Object object) {
        writer.accept(object);
    }

    /**
     * @return the unique identifier of this connection.
     */
    public String id() {
        return properties.get(ID);
    }

    /**
     * Adds a listener for the close event.
     *
     * @param closeHandler called after the connection is closed.
     * @return fluent.
     */
    public Connection onClose(Runnable closeHandler) {
        closeHandlers.add(closeHandler);
        return this;
    }

    /**
     * closes the connection and committing any pending messages.
     */
    public void close() {
        closeHandlers.forEach(Runnable::run);
    }

    /**
     * Retrieves a value from the connection properties.
     *
     * @param key the property to retrieve.
     * @return an optional of the property value.
     */
    public Optional<String> getProperty(String key) {
        return Optional.ofNullable(properties.getOrDefault(key, null));
    }

    /**
     * Sets a property that will exist for the connections duration.
     *
     * @param key   the key of the property to set.
     * @param value the value of the property to set.
     * @return fluent.
     */
    public Connection setProperty(String key, String value) {
        properties.put(key, value);
        return this;
    }
}
