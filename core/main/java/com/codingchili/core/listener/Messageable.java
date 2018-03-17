package com.codingchili.core.listener;

/**
 * Defines a set of operations supported by the messaging implementations.
 */
public interface Messageable {

    /**
     * Writes an object to the connection that backs the current request.
     *
     * @param object the object to be written.
     */
    void write(Object object);
}
