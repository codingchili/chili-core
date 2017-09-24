package com.codingchili.core.files.exception;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 * <p>
 * Throw when a requested configuration is missing.
 */
public class InvalidConfigurationPath extends CoreRuntimeException {
    /**
     * @param clazz the class of the configurable that is missing.
     */
    public InvalidConfigurationPath(Class clazz) {
        super(CoreStrings.getIllegalPathToConfigurable(clazz));
    }
}
