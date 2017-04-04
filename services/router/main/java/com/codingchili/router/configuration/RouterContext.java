package com.codingchili.router.configuration;

import static com.codingchili.router.configuration.RouterSettings.PATH_ROUTING;

import java.util.List;

import com.codingchili.core.context.ServiceContext;
import com.codingchili.core.files.Configurations;
import com.codingchili.router.model.WireType;

import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 *         <p>
 *         A context for the router service.
 */
public class RouterContext extends ServiceContext {

    public RouterContext(Vertx vertx) {
        super(vertx);
    }

    public RouterSettings service() {
        return Configurations.get(PATH_ROUTING, RouterSettings.class);
    }

    public boolean isRouteHidden(String target) {
        return service().getHidden().contains(target);
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
