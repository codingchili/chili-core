package com.codingchili.logging;


import com.codingchili.logging.configuration.LogContext;
import com.codingchili.logging.controller.ClientLogHandler;
import com.codingchili.logging.controller.ServiceLogHandler;
import io.vertx.core.*;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.CoreService;

/**
 * @author Robin Duda
 *         Receives logging data from the other components and writes it to an elasticsearch cluster or logger.
 */
public class Service implements CoreService {
    private LogContext context;

    @Override
    public void init(CoreContext core) {
        this.context = new LogContext(core);
    }

    @Override
    public void stop(Future<Void> stop) {
        context.logger().onServiceStopped(stop);
    }

    @Override
    public void start(Future<Void> start) {
        for (int i = 0; i < context.system().getHandlers(); i++) {
            context.handler(new ServiceLogHandler(context), done -> {});
            context.handler(new ClientLogHandler(context), done -> {});
        }
        context.logger().onServiceStarted(start);
    }
}
