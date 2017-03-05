package com.codingchili.router.configuration;

import com.codingchili.router.model.WireType;
import io.vertx.core.Vertx;

import java.util.List;

import com.codingchili.core.context.ServiceContext;
import com.codingchili.core.files.Configurations;

import static com.codingchili.router.configuration.RouterSettings.PATH_ROUTING;

/**
 * @author Robin Duda
 */
public class RouterContext extends ServiceContext {

    public RouterContext(Vertx vertx) {
        super(vertx);
    }

    protected RouterSettings service() {
        return Configurations.get(PATH_ROUTING, RouterSettings.class);
    }

    public boolean isRouteHidden(String target) {
        return service().getHidden().contains(target);
    }

    public List<ListenerSettings> transports() {
        return service().getTransport();
    }

    public ListenerSettings getListener(WireType type) {
        return service().getListener(type);
    }
}
