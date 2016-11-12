package com.codingchili.core.Exception;

import com.codingchili.core.Configuration.Strings;

/**
 * @author Robin Duda
 */
public class SystemNotInitializedException extends RuntimeException {
    public SystemNotInitializedException(Class clazz) {
        super(Strings.getSystemNotInitialized(clazz.getSimpleName()));
    }
}
