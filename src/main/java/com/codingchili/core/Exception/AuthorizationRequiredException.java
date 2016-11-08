package com.codingchili.core.Exception;

import static com.codingchili.core.Configuration.Strings.ERROR_NOT_AUTHORIZED;

/**
 * @author Robin Duda
 */
public class AuthorizationRequiredException extends CoreException {

    public AuthorizationRequiredException() {
        super(ERROR_NOT_AUTHORIZED);
    }

}
