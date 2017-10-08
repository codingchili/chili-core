package com.codingchili.core.listener.transport;

import com.codingchili.core.context.SystemContext;
import com.codingchili.router.configuration.RouterContext;
import com.codingchili.router.configuration.RouterSettings;

/**
 * @author Robin Duda
 */
public class ContextMock extends RouterContext {
    private RouterSettings settings;


    public ContextMock() {
        super(new SystemContext());
    }

    public ContextMock setSettings(RouterSettings settings) {
        this.settings = settings;
        return this;
    }

    @Override
    public RouterSettings service() {
        return settings;
    }
}
