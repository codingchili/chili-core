package com.codingchili.core.listener;

/**
 * Listeners handles incoming messages and forwards them to a handler.
 */
public interface CoreListener extends CoreDeployment {

    /**
     * @param settings listener settings for the listener.
     * @return fluent
     */
    CoreListener settings(ListenerSettings settings);

    /**
     * @param handler the handler to invoke when the listener is triggered
     *                the handler must be initialized with the current context by
     *                the implementing class.
     * @return fluent
     */
    CoreListener handler(CoreHandler<?> handler);

    /**
     * @return the name of the listener.
     */
    default String name() {
        return getClass().getSimpleName();
    }
}
