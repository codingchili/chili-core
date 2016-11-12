package com.codingchili.core.Exception;

import com.codingchili.core.Configuration.Strings;

/**
 * @author Robin Duda
 *
 * Throw when a request has failed validation.
 */
public class RequestValidationException extends CoreException {

    public RequestValidationException() {
        super(Strings.ERROR_VALIDATION_FAILURE);
    }
}
