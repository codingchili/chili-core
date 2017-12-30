package com.codingchili.logging.controller;

import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;
import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.protocol.Protocol;
import com.codingchili.core.protocol.exception.HandlerMissingException;
import com.codingchili.logging.configuration.LogContext;
import com.codingchili.logging.model.StorageLogger;

import static com.codingchili.common.Strings.ID_PING;
import static com.codingchili.common.Strings.PROTOCOL_LOGGING;
import static com.codingchili.core.protocol.Role.PUBLIC;

/**
 * @author Robin Duda
 * <p>
 * Base log handler to receive remote logging events.
 */
abstract class AbstractLogHandler implements CoreHandler {
    final ConsoleLogger console;
    final StorageLogger store;
    private final Protocol<Request> protocol = new Protocol<>();
    private String address;
    protected LogContext context;

    AbstractLogHandler(LogContext context, String address) {
        this.context = context;
        this.address = address;

        console = new ConsoleLogger(getClass());
        store = new StorageLogger(context, getClass());

        protocol.setRole(PUBLIC)
                .use(PROTOCOL_LOGGING, this::log)
                .use(ID_PING, Request::accept);
    }

    @Override
    public void handle(Request request) {
        try {
            protocol.get(request.route()).submit(request);
        } catch (HandlerMissingException e) {
            console.onHandlerMissing(request.target(), request.route());
            store.onHandlerMissing(request.target(), request.route());
        }
    }

    @Override
    public String address() {
        return address;
    }

    protected abstract void log(Request request);
}
