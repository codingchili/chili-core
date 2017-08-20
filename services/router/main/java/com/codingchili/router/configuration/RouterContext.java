package com.codingchili.router.configuration;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.ServiceContext;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.ListenerSettings;
import com.codingchili.core.listener.WireType;
import io.vertx.core.Vertx;

import java.util.List;

import static com.codingchili.router.configuration.RouterSettings.PATH_ROUTING;

/**
 * @author Robin Duda
 *         <p>
 *         A context for the router service.
 */
public class RouterContext extends ServiceContext {

    public RouterContext(Vertx vertx) {
        super(vertx);
    }

    public RouterContext(CoreContext core) {
        super(core);
    }

    public RouterSettings service() {
        return Configurations.get(PATH_ROUTING, RouterSettings.class);
    }

    public boolean isRouteHidden(String target) {
        return service().isHidden(target);
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
