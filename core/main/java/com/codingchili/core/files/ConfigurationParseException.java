package com.codingchili.core.files;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * Thrown when configuration has failed to parse.
 */
public class ConfigurationParseException extends CoreRuntimeException {

    /**
     * @param filePath  the path to the file that failed to parse.
     * @param cause the original exception.
     */
    public ConfigurationParseException(String filePath, Throwable cause) {
        super(String.format("Failure parsing configuration file %s", filePath), cause);
    }
}
