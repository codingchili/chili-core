package com.codingchili.core.context.exception;

import com.codingchili.core.configuration.CoreStrings;

/**
 * @author Robin Duda
 */
public class SystemNotInitializedException extends RuntimeException {
    public SystemNotInitializedException(Class clazz) {
        super(CoreStrings.getSystemNotInitialized(clazz.getSimpleName()));
    }
}
