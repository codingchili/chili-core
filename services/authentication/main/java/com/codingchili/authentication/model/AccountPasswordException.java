package com.codingchili.authentication.model;

import com.codingchili.core.protocol.ResponseStatus;

/**
 * @author Robin Duda
 * <p>
 * Throw when authentication fails.
 */
public class AccountPasswordException extends AccountException {
    protected AccountPasswordException() {
        super("failed to authenticate with given password", ResponseStatus.UNAUTHORIZED);
    }
}
