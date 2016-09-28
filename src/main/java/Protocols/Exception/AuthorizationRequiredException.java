package Protocols.Exception;

import Configuration.Strings;

/**
 * @author Robin Duda
 */
public class AuthorizationRequiredException extends Exception {

    public AuthorizationRequiredException() {
        super(Strings.ERROR_NOT_AUTHORIZED);
    }

}
