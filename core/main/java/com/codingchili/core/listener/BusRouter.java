package com.codingchili.core.listener;

import io.vertx.core.eventbus.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.logging.Level;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.Address;
import com.codingchili.core.protocol.exception.*;

import static com.codingchili.core.configuration.CoreStrings.*;
import static io.vertx.core.eventbus.ReplyFailure.*;

/**
 * Simple handler that routes messages to the event bus and
 * passes eventbus errors back to sender.
 */
@Address("dynamic")
public class BusRouter implements CoreHandler<Request> {
    private final Map<ReplyFailure, Consumer<Request>> exceptionHandlers = new HashMap<>();
    private Logger logger;
    private CoreContext core;

    @Override
    public void init(CoreContext core) {
        this.core = core;
        this.logger = core.logger(getClass());
        exceptionHandlers.put(TIMEOUT, this::onNodeTimeout);
        exceptionHandlers.put(NO_HANDLERS, this::onNodeNotReachable);
        exceptionHandlers.put(RECIPIENT_FAILURE, this::onRecipientFailure);
    }

    @Override
    public void handle(Request request) {
        if (ID_PING.equals(request.target())) {
            request.accept();
        } else {
            send(request, request.target());
        }
    }

    protected void send(Request request, String target) {
        DeliveryOptions options = new DeliveryOptions().setSendTimeout(request.timeout());

        core.bus().request(target, request.data(), options, send -> {
            if (send.succeeded()) {
                request.write(send.result().body());
            } else {
                Throwable exception = send.cause();

                if (exception instanceof ReplyException) {
                    ReplyFailure status = ((ReplyException) exception).failureType();
                    exceptionHandlers.get(status).accept(request);
                } else {
                    request.error(send.cause());
                }
            }
        });
    }

    protected void onRecipientFailure(Request request) {
        request.error(new NodeFailedToAcknowledge(request));
        logger.event(LOG_NODE_FAILURE, Level.WARNING)
                .put(PROTOCOL_TARGET, request.target())
                .put(PROTOCOL_ROUTE, request.route())
                .send(getNodeFailedToAcknowledge(request.target(), request.route()));
    }

    protected void onNodeNotReachable(Request request) {
        request.error(new NodeNotReachableException(request));
        logger.event(LOG_NODE_UNREACHABLE, Level.ERROR)
                .put(PROTOCOL_TARGET, request.target())
                .put(PROTOCOL_ROUTE, request.route())
                .send(getNodeNotReachable(request.target(), request.route()));
    }

    protected void onNodeTimeout(Request request) {
        request.error(new RequestTimedOutException(request));
        logger.event(LOG_NODE_TIMEOUT, Level.WARNING)
                .put(PROTOCOL_TARGET, request.target())
                .put(PROTOCOL_ROUTE, request.route())
                .send(getServiceTimeout(request.target(), request.route(), request.timeout()));
    }
}
