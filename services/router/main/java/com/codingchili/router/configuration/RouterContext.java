package com.codingchili.router.configuration;

import static com.codingchili.common.Strings.*;
import static com.codingchili.core.configuration.CoreStrings.PROTOCOL_ROUTE;
import static com.codingchili.core.configuration.CoreStrings.PROTOCOL_TARGET;
import static com.codingchili.router.configuration.RouterSettings.PATH_ROUTING;

import java.util.List;

import com.codingchili.core.context.ServiceContext;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.Level;

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

    public void onNodeTimeout(String target, String route, int timeout) {
        logger.log(event(LOG_NODE_TIMEOUT, Level.WARNING)
                .put(PROTOCOL_TARGET, target)
                .put(PROTOCOL_ROUTE, route)
                .put(ID_MESSAGE, getServiceTimeout(target, route, timeout)));
    }

    public void onNodeNotReachable(String target) {
        logger.log(event(LOG_NODE_UNREACHABLE, Level.SEVERE)
                .put(PROTOCOL_TARGET, target)
                .put(ID_MESSAGE, getNodeNotReachable(target)));
    }

    public void onRecipientFailure(String target, String route) {
        logger.log(event(LOG_NODE_FAILURE, Level.WARNING)
                .put(PROTOCOL_TARGET, target)
                .put(PROTOCOL_ROUTE, route)
                .put(ID_MESSAGE, getNodeFailedToAcknowledge(target, route)));
    }
}
