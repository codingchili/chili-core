package com.codingchili.core.listener.transport;

import java.util.*;
import java.util.function.Consumer;

import com.codingchili.core.listener.Messageable;

import static com.codingchili.core.configuration.CoreStrings.PROTOCOL_CONNECTION;

/**
 * @author Robin Duda
 * <p>
 * Represents the stateful connection over which requests may be passed.
 * For stateless requests such as ClusterRequest and RestRequest the
 * client may only live during a single request.
 */
public class Connection implements Messageable {
    private Map<String, String> properties = new HashMap<>();
    private Map<String, Runnable> closeHandlers = new HashMap<>();
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
     * A textual representation of the sending party. This is typically the IP address
     * retrieved from the underlying connection.
     *
     * @return the sending party represented as a string.
     */
    public String remote() {
        return getProperty(PROTOCOL_CONNECTION).orElse("");
    }

    /**
     * Creates a unnamed close handler that cannot be removed.
     *
     * @param runnable called after the connection is closed.
     * @return fluent.
     */
    public Connection onCloseHandler(Runnable runnable) {
        onCloseHandler(UUID.randomUUID().toString(), runnable);
        return this;
    }

    /**
     * Adds a listener for the close event.
     *
     * @param name the name of the close handler so that it can be removed or updated.
     * @param closeHandler called after the connection is closed.
     * @return fluent.
     */
    public Connection onCloseHandler(String name, Runnable closeHandler) {
        closeHandlers.put(name, closeHandler);
        return this;
    }

    /**
     * @param name the name of the close handler to remove.
     * @return fluent.
     */
    public Connection removeCloseHandler(String name) {
        closeHandlers.remove(name);
        return this;
    }

    /**
     * executes the close handlers registered on the connection. Does not actually
     * close the connection itself. This method is primarily intended to be called by
     * the listener which created the connection.
     */
    public void runCloseHandlers() {
        closeHandlers.values().forEach(Runnable::run);
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
