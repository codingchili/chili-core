package com.codingchili.authentication.model;

import com.codingchili.core.protocol.ResponseStatus;

/**
 * @author Robin Duda
 * <p>
 * Thrown when an user already exists.
 */
public class AccountExistsException extends AccountException {
    protected AccountExistsException() {
        super("the account already exists", ResponseStatus.CONFLICT);
    }
}
