package Protocols;


import Logging.Model.Logger;
import Protocols.Exception.AuthorizationRequiredException;
import Protocols.Exception.HandlerMissingException;

/**
 * @author Robin Duda
 */
public abstract class HandlerProvider {
    protected Logger logger;
    protected Protocol protocol;

    public void process(Request request) {
        try {
            protocol.handle(this, request);
        } catch (AuthorizationRequiredException authorizationRequired) {
            request.unauthorized();
        } catch (HandlerMissingException e) {
            request.missing();

            logger.onHandlerMissing(request.action());
        }
    }
}
