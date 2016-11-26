package com.codingchili.services.logging;


import io.vertx.core.*;

import com.codingchili.core.protocol.ClusterNode;
import com.codingchili.core.context.Deploy;

import com.codingchili.services.logging.configuration.LogContext;
import com.codingchili.services.logging.controller.ClientLogHandler;
import com.codingchili.services.logging.controller.ServiceLogHandler;

/**
 * @author Robin Duda
 *         Receives logging data from the other components and writes it to an elasticsearch cluster or logger.
 */
public class Service extends ClusterNode {

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        this.vertx = vertx;
    }

    @Override
    public void start(Future<Void> start) {
        LogContext context = new LogContext(vertx);

        for (int i = 0; i < settings.getHandlers(); i++) {
            Deploy.service(new ServiceLogHandler<>(context));
            Deploy.service(new ClientLogHandler<>(context));
        }

        start.complete();
    }
}
