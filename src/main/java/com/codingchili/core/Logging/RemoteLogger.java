package com.codingchili.core.Logging;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.Context.CoreContext;

import static com.codingchili.services.Shared.Strings.NODE_LOGGING;

/**
 * @author Robin Duda
 */
public class RemoteLogger extends DefaultLogger {
    private final DeliveryOptions options;

    public RemoteLogger(CoreContext context) {
        super(context);
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
