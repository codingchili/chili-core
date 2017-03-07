package com.codingchili.core.context;

import com.codingchili.core.protocol.ResponseStatus;

/**
 * @author Robin Duda
 *         <p>
 *         Exceptions should extend this class to allow for catching all core-type exceptions.
 *         Additionally, all classes extending CoreException must be safe to be forwarded to clients.
 */
public class CoreException extends Exception {
    private ResponseStatus status = ResponseStatus.ERROR;

    protected CoreException(String error) {
        super(error);
    }

    protected CoreException(String error, ResponseStatus status) {
        super(error);
        this.status = status;
    }

    public ResponseStatus status() {
        return status;
    }

}
