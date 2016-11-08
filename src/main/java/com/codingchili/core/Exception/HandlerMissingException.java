package com.codingchili.core.Exception;

import com.codingchili.core.Configuration.Strings;

/**
 * @author Robin Duda
 */
public class HandlerMissingException extends CoreException {

    public HandlerMissingException() {
        super(Strings.ERROR_HANDLER_MISSING);
    }

}
