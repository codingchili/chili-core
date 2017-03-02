package com.codingchili.logging.controller;

import com.codingchili.logging.configuration.LogContext;
import com.codingchili.logging.model.StorageLogger;

import com.codingchili.core.context.CoreException;
import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.protocol.*;
import com.codingchili.core.protocol.exception.HandlerMissingException;

import static com.codingchili.common.Strings.*;
import static com.codingchili.core.protocol.Access.*;

/**
 * @author Robin Duda
 *         <p>
 *         Base log handler to receive remote logging events.
 */
abstract class AbstractLogHandler<T extends LogContext> extends AbstractHandler<T> {
    private final Protocol<RequestHandler<Request>> protocol = new Protocol<>();
    final ConsoleLogger console;
    final StorageLogger store;

    AbstractLogHandler(T context, String address) {
        super(context, address);

        console = new ConsoleLogger(context);
        store = new StorageLogger(context);

        protocol.use(PROTOCOL_LOGGING, this::log)
                .use(ID_PING, Request::accept, PUBLIC);
    }

    @Override
    public void handle(Request request) throws CoreException {
        try {
            protocol.get(AUTHORIZED, request.route()).handle(request);
        } catch (HandlerMissingException e) {
            console.onHandlerMissing(request.route());
            store.onHandlerMissing(request.route());
        }
    }

    protected abstract void log(Request request);
}
