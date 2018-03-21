package com.codingchili.router.configuration;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.ServiceContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.ListenerSettings;
import com.codingchili.core.listener.WireType;

import java.util.List;

import static com.codingchili.router.configuration.RouterSettings.PATH_ROUTING;

/**
 * @author Robin Duda
 * <p>
 * A context for the router service.
 */
public class RouterContext extends SystemContext implements ServiceContext {

    public RouterContext(CoreContext core) {
        super(core);
    }

    public RouterSettings service() {
        return Configurations.get(PATH_ROUTING, RouterSettings.class);
    }

    public boolean isRouteExternal(String target, String route) {
        return service().isExternal(target, route);
    }

    public List<ListenerSettings> transports() {
        return service().getTransport();
    }

    /**
     * @param type the type of listener to get configuration for
     * @return listener configuration if existing.
     */
    public ListenerSettings getListener(WireType type) {
        return service().getListener(type);
    }
}
