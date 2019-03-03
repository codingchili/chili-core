package com.codingchili.core.configuration.exception;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreException;

/**
 * Throw when a change has been made to a configuration file that cannot
 * be applied to runtime.
 */
public class ConfigurationMismatchException extends CoreException {
    public ConfigurationMismatchException() {
        super(CoreStrings.ERROR_CONFIGURATION_MISMATCH);
    }
}
