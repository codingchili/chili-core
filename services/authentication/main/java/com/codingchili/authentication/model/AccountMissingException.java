package com.codingchili.authentication.model;

import com.codingchili.core.protocol.ResponseStatus;

/**
 * @author Robin Duda
 * <p>
 * Thrown when an account is not found.
 */
public class AccountMissingException extends AccountException {
    protected AccountMissingException() {
        super("requested account is missing.", ResponseStatus.MISSING);
    }
}
