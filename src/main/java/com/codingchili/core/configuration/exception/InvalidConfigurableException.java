package com.codingchili.core.configuration.exception;

import com.codingchili.core.configuration.Strings;

/**
 * @author Robin Duda
 *
 * Throw when the given class is not a valid configurable.
 */
public class InvalidConfigurableException extends RuntimeException {
    public InvalidConfigurableException(Class<?> clazz) {
        super(Strings.getErrorInvalidConfigurable(clazz));
    }
}
