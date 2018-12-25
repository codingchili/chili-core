package com.codingchili.core.testing;

import com.codingchili.core.configuration.ServiceConfigurable;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.ServiceContext;
import com.codingchili.core.context.SystemContext;

/**
 * @author Robin Duda
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
