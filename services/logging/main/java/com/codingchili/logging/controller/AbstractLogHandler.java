package com.codingchili.logging.controller;

import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;
import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.protocol.Protocol;
import com.codingchili.core.protocol.RequestHandler;
import com.codingchili.core.protocol.exception.HandlerMissingException;
import com.codingchili.logging.configuration.LogContext;
import com.codingchili.logging.model.StorageLogger;

import static com.codingchili.common.Strings.*;
import static com.codingchili.core.protocol.Access.*;

/**
 * @author Robin Duda
 *         <p>
 *         Base log handler to receive remote logging events.
 */
abstract class AbstractLogHandler implements CoreHandler {
    private final Protocol<RequestHandler<Request>> protocol = new Protocol<>();
    final ConsoleLogger console;
    final StorageLogger store;
    LogContext context;
    private String address;

    AbstractLogHandler(LogContext context, String address) {
        this.context = context;
        this.address = address;

        console = new ConsoleLogger(context);
        store = new StorageLogger(context);

        protocol.use(PROTOCOL_LOGGING, this::log)
                .use(ID_PING, Request::accept, PUBLIC);
    }

    @Override
    public void handle(Request request) {
        try {
            protocol.get(AUTHORIZED, request.route()).handle(request);
        } catch (HandlerMissingException e) {
            console.onHandlerMissing(request.route());
            store.onHandlerMissing(request.route());
        }
    }

    @Override
    public String address() {
        return address;
    }

    protected abstract void log(Request request);
}
