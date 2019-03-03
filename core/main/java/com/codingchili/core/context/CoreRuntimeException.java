package com.codingchili.core.context;

import com.codingchili.core.context.exception.CoreExceptionFormat;
import com.codingchili.core.protocol.ResponseStatus;

/**
 * Runtime exception.
 */
public class CoreRuntimeException extends RuntimeException implements CoreExceptionFormat {
    private ResponseStatus status = ResponseStatus.ERROR;

    /**
     * @param description of the generated error and the cause.
     */
    public CoreRuntimeException(String description) {
        super(description, null, false, false);
    }

    /**
     * creates a runtime exception with a stacktrace.
     *
     * @param description why we are throwing an exception
     * @param cause       the original exception.
     */
    public CoreRuntimeException(String description, Throwable cause) {
        super(description, cause);
    }

    /**
     * @param cause the original exception.
     */
    public CoreRuntimeException(Throwable cause) {
        super(cause);
    }

    /**
     * @param error  description of the error and the cause.
     * @param status the error level.
     */
    public CoreRuntimeException(String error, ResponseStatus status) {
        super(error, null, false, false);
        this.status = status;
    }

    @Override
    public ResponseStatus status() {
        return status;
    }
}
