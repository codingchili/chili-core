package com.codingchili.core.protocol.exception;

import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.protocol.ResponseStatus;

import static com.codingchili.core.configuration.CoreStrings.getHandlerMissing;

/**
 * Throw when a requested handler does not exist.
 */
public class HandlerMissingException extends CoreRuntimeException {

    public HandlerMissingException(String handler) {
        super(getHandlerMissing(handler), ResponseStatus.ERROR);
    }
}
