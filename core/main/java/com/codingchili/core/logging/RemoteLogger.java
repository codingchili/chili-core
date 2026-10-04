package com.codingchili.core.logging;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.files.Configurations;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * A logger that logs to a remote host.
 */
public class RemoteLogger extends AbstractLogger {
    private DeliveryOptions options = new DeliveryOptions().setSendTimeout(8000);
    private ConsoleLogger console = new ConsoleLogger(aClass);

    public RemoteLogger(CoreContext context, Class aClass) {
        super(context, aClass);
        this.context = context;
    }

    @Override
    public Logger log(JsonObject data) {
        boolean sent = true;
        try {
            context.bus().send(NODE_LOGGING, new JsonObject()
                    .put(PROTOCOL_ROUTE, PROTOCOL_LOGGING)
                    .put(PROTOCOL_TARGET, NODE_LOGGING)
                    .put(PROTOCOL_MESSAGE, data), options);
        } catch (RuntimeException e) {
            // logging must not fail: the event bus is closed (or closing) when a context is shut down.
            sent = false;
        }

        if (!sent || Configurations.system().isConsoleLogging()) {
            console.log(data);
        }
        return this;
    }

    @Override
    public void close() throws SecurityException {
        console.close();
    }
}
