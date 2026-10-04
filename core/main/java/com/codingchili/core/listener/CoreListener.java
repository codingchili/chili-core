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
    /**
     * The class of the configuration of the transport that the listener reads from its settings, see
     * {@link ListenerSettings#config(Class, java.util.function.Supplier)}. The configuration is the {@code config}
     * property of the listener settings, in the configuration file.
     *
     * @return the configuration class, or null if the listener has no configuration of its transport.
     */
    default Class<?> configType() {
        return null;
    }

    default String name() {
        return getClass().getSimpleName();
    }
}
