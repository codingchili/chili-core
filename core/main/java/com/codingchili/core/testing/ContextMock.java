package com.codingchili.core.testing;

import com.codingchili.core.configuration.ServiceConfigurable;
import com.codingchili.core.context.*;

/**
 * A simple mock class.
 */
public class ContextMock extends SystemContext implements ServiceContext {

    public ContextMock(CoreContext context) {
        super(context);
    }

    public ContextMock() {
        super();
    }

    public ServiceConfigurable service() {
        return new ServiceConfigurable() {
        };
    }
}
