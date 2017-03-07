package com.codingchili.core.protocol.exception;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreException;
import com.codingchili.core.protocol.ResponseStatus;

/**
 * @author Robin Duda
 *         <p>
 *         Throw when a requested handler does not exist.
 */
public class HandlerMissingException extends CoreException {

    public HandlerMissingException() {
        super(CoreStrings.ERROR_HANDLER_MISSING, ResponseStatus.ERROR);
    }
}
