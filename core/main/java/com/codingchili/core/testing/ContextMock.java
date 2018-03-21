package com.codingchili.core.testing;

import com.codingchili.core.configuration.ServiceConfigurable;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.CoreServiceContext;
import com.codingchili.core.context.SystemContext;

/**
 * @author Robin Duda
 */
public class ContextMock extends SystemContext implements CoreServiceContext {

    public ContextMock(CoreContext context) {
        super(context);
    }

    public ContextMock() {
        super();
    }

    @Override
    public ServiceConfigurable service() {
        return new ServiceConfigurable() {
        };
    }
}
