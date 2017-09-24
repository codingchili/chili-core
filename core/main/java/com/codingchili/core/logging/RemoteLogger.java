package com.codingchili.core.logging;

import com.codingchili.core.context.ServiceContext;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;

import static com.codingchili.core.configuration.CoreStrings.NODE_LOGGING;

/**
 * @author Robin Duda
 * <p>
 * A logger that logs to a remote host.
 */
public class RemoteLogger extends DefaultLogger {
    private final DeliveryOptions options;
    private ConsoleLogger console = new ConsoleLogger();

    public RemoteLogger(ServiceContext context) {
        super(context);
        this.context = context;
        this.options = new DeliveryOptions().setSendTimeout(8000);
    }

    @Override
    public Logger log(JsonObject data) {
        console.log(data);
        context.bus().send(NODE_LOGGING, data, options);
        return this;
    }
}
