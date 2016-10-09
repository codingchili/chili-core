package com.codingchili.core.Protocols.Exception;

import com.codingchili.core.Configuration.Strings;

/**
 * @author Robin Duda
 */
public class AuthorizationRequiredException extends ProtocolException {

    public AuthorizationRequiredException() {
        super(Strings.ERROR_NOT_AUTHORIZED);
    }

}
