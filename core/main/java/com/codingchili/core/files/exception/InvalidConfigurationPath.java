package com.codingchili.core.files.exception;

import com.codingchili.core.configuration.*;
import com.codingchili.core.context.*;

/**
 * @author Robin Duda
 *         <p>
 *         Throw when a requested configuration is missing.
 */
public class InvalidConfigurationPath extends CoreRuntimeException {
    /**
     * @param clazz the class of the configurable that is missing.
     */
    public InvalidConfigurationPath(Class clazz) {
        super(CoreStrings.getIllegalPathToConfigurable(clazz));
    }
}
