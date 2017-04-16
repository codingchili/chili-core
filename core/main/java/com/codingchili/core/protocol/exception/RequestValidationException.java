package com.codingchili.core.protocol.exception;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreException;
import com.codingchili.core.protocol.ResponseStatus;

/**
 * @author Robin Duda
 *         <p>
 *         Throw when a request has error validation.
 */
public class RequestValidationException extends CoreException {

    public RequestValidationException() {
        super(CoreStrings.ERROR_VALIDATION_FAILURE, ResponseStatus.BAD);
    }
}
