package com.codingchili.core.Exception;

import com.codingchili.core.Configuration.Strings;

/**
 * @author Robin Duda
 *
 * Throw when a change has been made to a configuration file that cannot
 * be applied to runtime.
 */
public class ConfigurationMismatchException extends CoreException {
    public ConfigurationMismatchException() {
        super(Strings.ERROR_CONFIGURATION_MISMATCH);
    }
}
