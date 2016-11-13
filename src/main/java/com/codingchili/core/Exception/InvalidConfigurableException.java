package com.codingchili.core.Exception;

import com.codingchili.core.Configuration.Strings;

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
