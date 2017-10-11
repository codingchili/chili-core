package com.codingchili.core.logging;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.files.Configurations;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * @author Robin Duda
 * <p>
 * A logger that logs to a remote host.
 */
public class RemoteLogger extends DefaultLogger {
    private DeliveryOptions options = new DeliveryOptions().setSendTimeout(8000);
    private ConsoleLogger console = new ConsoleLogger(aClass);

    public RemoteLogger(CoreContext context, Class aClass) {
        super(context, aClass);
        this.context = context;
    }

    @Override
    public Logger log(JsonObject data) {
        if (Configurations.system().isConsoleLogging()) {
            console.log(data);
        }
        context.bus().send(NODE_LOGGING, new JsonObject()
                .put(PROTOCOL_ROUTE, PROTOCOL_LOGGING)
                .put(PROTOCOL_TARGET, NODE_LOGGING)
                .put(PROTOCOL_MESSAGE, data), options);
        return this;
    }
}
