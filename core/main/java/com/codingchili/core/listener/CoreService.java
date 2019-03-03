package com.codingchili.core.listener;

/**
 * Specifies a handler bootstrapper.
 * <p>
 * A service initializes a set of handlers.
 */
public interface CoreService extends CoreDeployment {

    /**
     * @return the name of the service for logging purposes.
     */
    default String name() {
        return getClass().getName();
    }
}
