package com.codingchili.core.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * Allows a subscriber to wait for the application context to become available.
 */
public class StartupListener {
    private static Collection<Consumer<CoreContext>> listeners = new ArrayList<>();
    private static CoreContext core;

    /**
     * Adds a subcriber that will be notified when the application context is loaded.
     * @param listener called on load or if already loaded.
     */
    public static void subscibe(Consumer<CoreContext> listener) {
        if (core != null) {
            listener.accept(core);
        } else {
            listeners.add(listener);
        }
    }

    /**
     * Calls all listeners once and removes them as listeners.
     * @param core the application context that was loaded.
     */
    public static void publish(CoreContext core) {
        StartupListener.core = core;
        if (core != null) {
            core.blocking(blocking -> {
                listeners.forEach(listener -> listener.accept(core));
                listeners.clear();
                blocking.complete();
            }, done -> {});
        }
    }
}
