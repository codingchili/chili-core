package com.codingchili.core.protocol.exception;

import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.protocol.ResponseStatus;

import static com.codingchili.core.configuration.CoreStrings.ERROR_NOT_AUTHORIZED;

/**
 * @author Robin Duda
 * <p>
 * Throw when authorization is required but was not possible, for example
 * when authentication is missing.
 */
public class AuthorizationRequiredException extends CoreRuntimeException {

    public AuthorizationRequiredException() {
        super(ERROR_NOT_AUTHORIZED, ResponseStatus.UNAUTHORIZED);
    }
}
