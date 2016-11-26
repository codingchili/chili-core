package com.codingchili.core.protocol.exception;

import com.codingchili.core.configuration.Strings;
import com.codingchili.core.context.CoreException;

/**
 * @author Robin Duda
 *
 * Throw when a requested handler does not exist.
 */
public class HandlerMissingException extends CoreException {

    public HandlerMissingException() {
        super(Strings.ERROR_HANDLER_MISSING);
    }

}
