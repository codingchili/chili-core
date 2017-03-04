package com.codingchili.core.protocol.exception;

import com.codingchili.core.context.CoreException;
import com.codingchili.core.protocol.ResponseStatus;

/**
 * @author Robin Duda
 *         <p>
 *         An exception that is safe to forward to clients.
 *         Never send any exception that might occur directly to clients.
 */
public class UnmappedException extends CoreException {
    public UnmappedException() {
        super("An unmapped exception occured on the server", ResponseStatus.ERROR);
    }
}
