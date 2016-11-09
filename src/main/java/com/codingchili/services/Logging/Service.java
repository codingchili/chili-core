package com.codingchili.services.Logging;


import io.vertx.core.*;

import com.codingchili.core.Protocol.ClusterNode;
import com.codingchili.core.Context.Deploy;

import com.codingchili.services.Logging.Configuration.LogContext;
import com.codingchili.services.Logging.Controller.ClientLogHandler;
import com.codingchili.services.Logging.Controller.ServiceLogHandler;

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
