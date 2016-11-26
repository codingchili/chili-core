package com.codingchili.core.context.exception;

import com.codingchili.core.configuration.Strings;

/**
 * @author Robin Duda
 */
public class SystemNotInitializedException extends RuntimeException {
    public SystemNotInitializedException(Class clazz) {
        super(Strings.getSystemNotInitialized(clazz.getSimpleName()));
    }
}
