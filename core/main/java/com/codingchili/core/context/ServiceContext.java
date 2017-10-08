package com.codingchili.core.context;

import com.codingchili.core.configuration.ServiceConfigurable;

/**
 * May be implemented by service specific contexts.
 */
public interface ServiceContext {
    /**
     * @return configuration for the service.
     */
    ServiceConfigurable service();
}
