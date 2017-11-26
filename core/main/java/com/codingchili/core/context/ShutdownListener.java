package com.codingchili.core.context;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Allows a subscriber to receive shutdown events.
 */
public class ShutdownListener {
    private static Collection<Runnable> listeners = new LinkedList<>();

    /**
     * Adds a shutdown listener that is called when the application context is closed.
     * @param runnable listener.
     */
    public static void subscribe(Runnable runnable) {
        listeners.add(runnable);
    }

    /**
     * @param core the context that was closed.
     */
    public static void publish(CoreContext core) {
        listeners.forEach(Runnable::run);
        listeners.clear();
    }
}
