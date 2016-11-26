package com.codingchili.services.logging.controller;

import com.codingchili.core.context.CoreException;
import com.codingchili.core.protocol.exception.HandlerMissingException;
import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.protocol.*;

import com.codingchili.services.logging.configuration.LogContext;
import com.codingchili.services.logging.model.ElasticLogger;

import static com.codingchili.services.Shared.Strings.*;
import static com.codingchili.core.protocol.Access.*;

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
            protocol.get(AUTHORIZED, request.route()).handle(request);
        } catch (HandlerMissingException e) {
            console.onHandlerMissing(request.route());
            elastic.onHandlerMissing(request.route());
        }
    }

    protected abstract void log(Request request);
}
