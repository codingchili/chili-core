package com.codingchili.core.context;

import com.codingchili.core.context.exception.CoreExceptionFormat;
import com.codingchili.core.protocol.ResponseStatus;

/**
 * Exceptions should extend this class to allow for catching all core-type exceptions.
 * Additionally, all classes extending CoreException must be safe to be forwarded to clients.
 */
public class CoreException extends Exception implements CoreExceptionFormat {
    private ResponseStatus status = ResponseStatus.ERROR;

    /**
     * @param error an error message.
     */
    protected CoreException(String error) {
        super(error);
    }

    /**
     * @param error  an error message.
     * @param status a status code.
     */
    protected CoreException(String error, ResponseStatus status) {
        super(error);
        this.status = status;
    }

    /**
     * @return the status code of the exception.
     */
    public ResponseStatus status() {
        return status;
    }
}
