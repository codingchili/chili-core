package com.codingchili.core.listener;

import com.codingchili.core.context.*;
import com.codingchili.core.protocol.exception.HandlerMissingException;
import com.codingchili.core.protocol.exception.RequestPayloadSizeException;

/**
 * @author Robin Duda
 *         <p>
 *         Handles incoming requests in an uniform way.
 */
public class RequestProcessor {
    /**
     * Handles an incoming request by calling the handlers handle method with the
     * given request. Handles missing handlers by logging to the contexts logger.
     *
     * @param request the request to be handled by the handler
     */
    public static void accept(CoreContext core, CoreHandler handler, Request request) {
        if (request.size() > request.maxSize()) {
            request.error(new RequestPayloadSizeException(request.maxSize()));
        } else {
            try {
                handler.handle(request);
            } catch (HandlerMissingException missing) {
                request.error(missing);
                core.logger().onHandlerMissing(request.route());
            } catch (CoreRuntimeException e) {
                request.error(e);
            }
        }
    }
}
