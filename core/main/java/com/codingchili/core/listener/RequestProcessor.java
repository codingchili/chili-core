package com.codingchili.core.listener;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.RequestHandler;
import com.codingchili.core.protocol.exception.HandlerMissingException;
import com.codingchili.core.protocol.exception.RequestPayloadSizeException;

/**
 * @author Robin Duda
 * <p>
 * Handles incoming requests with error processing and logging.
 */
public class RequestProcessor implements RequestHandler<Request> {
    private CoreContext context;
    private CoreHandler handler;
    private Logger logger;

    /**
     * @param context the context on which the processor runs on.
     * @param handler the handler that is used to handle requests.
     */
    public RequestProcessor(final CoreContext context, final CoreHandler handler) {
        this.context = context;
        this.handler = handler;
        this.logger = context.logger(getClass());
    }

    /**
     * Handles an incoming request by calling the handlers handle method with the
     * given request. Handles missing handlers by logging to the contexts logger.
     * Catches exceptions and writes them to the request and the contexts logger.
     *
     * @param request the request to be handled by the handler
     */
    @Override
    public void accept(final Request request) {
        if (request.size() > request.maxSize()) {
            request.error(new RequestPayloadSizeException(request.maxSize()));
        } else {
            try {
                request.init();
                handler.handle(request);
            } catch (HandlerMissingException missing) {
                request.error(missing);
                logger.onHandlerMissing(request.route());
            } catch (Throwable e) {
                request.error(e);
                logger.onError(e);
            }
        }
    }
}
