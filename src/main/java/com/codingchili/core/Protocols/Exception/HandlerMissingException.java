package com.codingchili.core.Protocols.Exception;

import com.codingchili.core.Configuration.Strings;

/**
 * @author Robin Duda
 */
public class HandlerMissingException extends Exception {

    public HandlerMissingException() {
        super(Strings.ERROR_HANDLER_MISSING);
    }

}
