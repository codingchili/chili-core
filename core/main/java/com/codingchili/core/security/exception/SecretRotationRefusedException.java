package com.codingchili.core.security.exception;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * Thrown when the generator refuses to rotate or retire inside the retire window of the last rotate: the previous
 * secrets may still verify tokens that are in use. The command can be repeated with --force.
 */
public class SecretRotationRefusedException extends CoreRuntimeException {

    /**
     * @param message why the generator refused, and what to do instead.
     */
    public SecretRotationRefusedException(String message) {
        super(message);
    }
}
