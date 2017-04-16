package com.codingchili.core.listener;

import java.util.function.Supplier;

/**
 * @author Robin Duda
 *         <p>
 *         Listeners handles incoming messages and forwards them to a handler.
 */
public interface CoreListener extends CoreLifecycle {

    /**
     * @param settings listener settings for the listener.
     * @return fluent
     */
    CoreListener settings(Supplier<ListenerSettings> settings);

    /**
     * @param handler the handler to invoke when the listener is triggered
     * @return fluent
     */
    CoreListener handler(CoreHandler handler);

    /**
     * @return the name of the listener.
     */
    default String name() {
        return getClass().getSimpleName();
    }
}
