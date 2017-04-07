package com.codingchili.core.logging;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.ServiceContext;

import static com.codingchili.core.configuration.CoreStrings.NODE_LOGGING;

/**
 * @author Robin Duda
 *
 * A logger that logs to a remote host.
 */
public class RemoteLogger extends DefaultLogger {
    private final DeliveryOptions options;

    public RemoteLogger(ServiceContext context) {
        super(context, context.identity().getNode());
        this.context = context;
        this.options = new DeliveryOptions()
                .setSendTimeout(8000);
    }

    @Override
    public Logger log(JsonObject data) {
        context.console().log(data);
        context.bus().send(NODE_LOGGING, data, options);
        return this;
    }
}
