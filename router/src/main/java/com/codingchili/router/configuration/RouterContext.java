package com.codingchili.router.configuration;

import io.vertx.core.Vertx;

import java.util.ArrayList;

import com.codingchili.core.context.ServiceContext;
import com.codingchili.core.files.Configurations;

import com.codingchili.router.model.WireType;

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

    public ArrayList<ListenerSettings> transports() {
        return service().getTransport();
    }

    public ListenerSettings getListener(WireType type) {
        return service().getListener(type);
    }


}
