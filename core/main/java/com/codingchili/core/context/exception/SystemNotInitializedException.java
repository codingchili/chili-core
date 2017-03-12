package com.codingchili.core.context.exception;

import com.codingchili.core.configuration.CoreStrings;

/**
 * @author Robin Duda
 *
 * Throw when a subsystem was used before it was initialized.
 */
public class SystemNotInitializedException extends RuntimeException {

    /**
     * @param clazz the system that was not initialized.
     */
    public SystemNotInitializedException(Class clazz) {
        super(CoreStrings.getSystemNotInitialized(clazz.getSimpleName()));
    }
}
