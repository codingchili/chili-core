package com.codingchili.services.Logging.Controller;

import com.codingchili.core.Exception.CoreException;
import com.codingchili.core.Exception.HandlerMissingException;
import com.codingchili.core.Logging.ConsoleLogger;
import com.codingchili.core.Protocol.*;

import com.codingchili.services.Logging.Configuration.LogContext;
import com.codingchili.services.Logging.Model.ElasticLogger;

import static com.codingchili.services.Shared.Strings.*;
import static com.codingchili.core.Protocol.Access.*;

/**
 * @author Robin Duda
 */
abstract class AbstractLogHandler<T extends LogContext> extends AbstractHandler<T> {
    private final Protocol<RequestHandler<Request>> protocol = new Protocol<>();
    final ConsoleLogger console;
    final ElasticLogger elastic;

    AbstractLogHandler(T context, String address) {
        super(context, address);

        console = new ConsoleLogger(context);
        elastic = new ElasticLogger(context);

        protocol.use(PROTOCOL_LOGGING, this::log)
                .use(ID_PING, Request::accept, PUBLIC);
    }

    @Override
    public void handle(Request request) throws CoreException {
        try {
            protocol.get(AUTHORIZED, request.action()).handle(request);
        } catch (HandlerMissingException e) {
            console.onHandlerMissing(request.action());
            elastic.onHandlerMissing(request.action());
        }
    }

    protected abstract void log(Request request);
}
