package com.codingchili.core.Protocols;

import com.codingchili.core.Logging.Model.Logger;
import com.codingchili.core.Protocols.Exception.AuthorizationRequiredException;
import com.codingchili.core.Protocols.Exception.HandlerMissingException;
import com.codingchili.core.Protocols.Exception.ProtocolException;
import io.vertx.core.Future;

/**
 * @author Robin Duda
 */
public abstract class AbstractHandler {
    protected Logger logger;
    private String address;

    protected AbstractHandler(String address) {
        this.address = address;
    }

    public void process(Request request) {
        try {
            handle(request);
        } catch (AuthorizationRequiredException e) {
            request.unauthorized();
        } catch (HandlerMissingException e) {
            logger.onHandlerMissing(request.action());
            request.error();
        } catch (ProtocolException e) {
            request.error();
        }
    }

    public abstract void handle(Request request) throws ProtocolException;

    public void stop(Future<Void> future) {}

    public void start(Future<Void> future) {}

    /**
     * Get the address of which the handler is providing handlers for.
     *
     * @return the address as a string representation.
     */
    String getAdddress() {
        return address;
    }
}
