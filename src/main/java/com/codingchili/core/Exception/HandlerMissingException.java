package com.codingchili.core.Exception;

import com.codingchili.core.Configuration.Strings;

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
