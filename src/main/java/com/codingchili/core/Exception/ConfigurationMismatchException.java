package com.codingchili.core.Exception;

import com.codingchili.core.Configuration.Strings;

/**
 * @author Robin Duda
 */
public class ConfigurationMismatchException extends CoreException {
    public ConfigurationMismatchException() {
        super(Strings.ERROR_CONFIGURATION_MISMATCH);
    }
}
