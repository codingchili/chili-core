package Protocols;


import Logging.Model.Logger;
import Protocols.Exception.AuthorizationRequiredException;
import Protocols.Exception.HandlerMissingException;

/**
 * @author Robin Duda
 */
public class HandlerProvider {
    protected Logger logger;
    protected Protocol protocol;
    private String address;

    protected HandlerProvider(Class clazz, Logger logger, String address) {
        this.protocol = new Protocol(clazz);
        this.logger = logger;
        this.address = address;
    }

    /**
     * Processes an incoming request with authentication control.
     * @param request the request to be processed.
     */
    public void process(Request request) {
        try {
            protocol.handle(this, request, request.action());
        } catch (AuthorizationRequiredException authorizationRequired) {
            request.unauthorized();
        } catch (HandlerMissingException e) {
            request.missing();

            logger.onHandlerMissing(request.action());
        }
    }

    /**
     * Get the address of which the handler is providing handlers for.
     * @return the address as a string representation.
     */
    public String getAdddress() {
        return address;
    }
}
