package com.codingchili.core.protocol.exception;

import com.codingchili.core.context.CoreException;

import static com.codingchili.core.configuration.CoreStrings.ERROR_NOT_AUTHORIZED;

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
