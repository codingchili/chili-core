package Protocols;


import Protocols.Exception.AuthorizationRequiredException;
import Protocols.Exception.HandlerMissingException;

/**
 * @author Robin Duda
 */
public interface HandlerProvider {
    void process(Request request, Access access) throws AuthorizationRequiredException, HandlerMissingException;
}
