package com.codingchili.authentication.model;

import com.codingchili.core.context.CoreException;
import com.codingchili.core.protocol.ResponseStatus;

/**
 * @author Robin Duda
 * <p>
 * Thrown when an authentication error has occured.
 */
abstract class AccountException extends CoreException {

    protected AccountException(String error, ResponseStatus status) {
        super(error, status);
    }
}
