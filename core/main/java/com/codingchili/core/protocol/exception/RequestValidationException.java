package com.codingchili.core.protocol.exception;

import com.codingchili.core.configuration.system.ValidatorSettings;
import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.protocol.ResponseStatus;

/**
 * Throw when a request has error validation.
 */
public class RequestValidationException extends CoreRuntimeException {

    /**
     * @param message the error message that caused validation error.
     */
    public RequestValidationException(String message) {
        super(message, ResponseStatus.BAD);
    }

    /**
     * Thrown when input validation fails due to length constraint.
     *
     * @param settings the settings object for the validation that failed.
     * @param length   the actual length of the input.
     * @return a new exception object.
     */
    public static RequestValidationException lengthError(ValidatorSettings settings, int length) {
        return new RequestValidationException(String.format("Expected %d-%d characters, got %d.",
            settings.getMinLength(), settings.getMaxLength(), length));
    }
}
