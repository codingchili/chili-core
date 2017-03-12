package com.codingchili.core.context.exception;

import com.codingchili.core.protocol.ResponseStatus;

/**
 * @author Robin Duda
 *
 * Base format for all core based exceptions.
 */
public interface CoreExceptionFormat {
    /**
     * @return the error status, most likely ERROR.
     */
    ResponseStatus status();
}
