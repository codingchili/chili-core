package com.codingchili.core.listener;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.exception.HandlerMissingException;
import com.codingchili.core.protocol.exception.RequestPayloadSizeException;

import java.util.function.Supplier;

/**
 * @author Robin Duda
 * <p>
 * Handles incoming requests with error processing and logging.
 */
public class RequestProcessor {
    private CoreHandler handler;
    private Logger logger;

    /**
     * @param context the context on which the processor runs on.
     * @param handler the handler that is used to handle requests.
     */
    public RequestProcessor(final CoreContext context, final CoreHandler handler) {
        this.handler = handler;
        this.logger = context.logger(getClass());
    }

    /**
     * Handles an incoming request by calling the handlers handle method with the
     * given request. Handles missing handlers by logging to the contexts logger.
     * Catches exceptions and writes them to the request and the contexts logger.
     * <p>
     * Uses a supplier so that instantiation can be done in this method
     * with error handling.
     *
     * @param supplier the request to be handled by the handler
     */
    public void submit(Supplier<Request> supplier) {
        Request request = null;
        try {
            request = supplier.get();
            if (request.size() > request.maxSize()) {
                request.error(new RequestPayloadSizeException(request.maxSize()));
            } else {
                handler.handle(request);
            }
        } catch (HandlerMissingException missing) {
            request = supplier.get();
            request.error(missing);
            logger.onHandlerMissing(request.target(), request.route());
        } catch (Throwable e) {
            if (request != null) {
                request.error(e);
            }
            logger.onError(e);
        }
    }
}
