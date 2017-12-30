package com.codingchili.logging;


import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.CoreService;
import com.codingchili.logging.configuration.LogContext;
import com.codingchili.logging.controller.ClientLogHandler;
import com.codingchili.logging.controller.ServiceLogHandler;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;

import static com.codingchili.core.context.FutureHelper.untyped;

/**
 * @author Robin Duda
 * Receives logging data from the other components and writes it to an elasticsearch cluster or logger.
 */
public class Service implements CoreService {
    private LogContext context;
    private Future<Void> future = Future.future();

    @Override
    public void init(CoreContext core) {
        this.context = new LogContext(core, future);
    }

    @Override
    public void start(Future<Void> start) {
        future.setHandler(done -> {
            CompositeFuture.all(
                    context.handler(() -> new ServiceLogHandler(context)),
                    context.handler(() -> new ClientLogHandler(context))
            ).setHandler(untyped(start));
        });
    }
}
