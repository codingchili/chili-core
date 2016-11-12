package com.codingchili.core.Exception;

import static com.codingchili.core.Configuration.Strings.ERROR_NOT_AUTHORIZED;

/**
 * @author Robin Duda
 *
 * Throw when authorization is required but was not possible, for example
 * when authentication is missing.
 */
public class AuthorizationRequiredException extends CoreException {

    public AuthorizationRequiredException() {
        super(ERROR_NOT_AUTHORIZED);
    }

}
