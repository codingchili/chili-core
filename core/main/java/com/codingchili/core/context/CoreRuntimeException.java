package com.codingchili.core.context;

import com.codingchili.core.context.exception.CoreExceptionFormat;
import com.codingchili.core.protocol.ResponseStatus;

/**
 * @author Robin Duda
 *
 * Runtime exception.
 */
public class CoreRuntimeException extends RuntimeException implements CoreExceptionFormat {
    private ResponseStatus status = ResponseStatus.ERROR;

    /**
     * @param description of the generated error and the cause.
     */
    protected CoreRuntimeException(String description) {
        super(description);
    }

    /**
     * @param error description of the error and the cause.
     * @param status the error level.
     */
    protected CoreRuntimeException(String error, ResponseStatus status) {
        super(error);
        this.status = status;
    }

    @Override
    public ResponseStatus status() {
        return status;
    }
}
