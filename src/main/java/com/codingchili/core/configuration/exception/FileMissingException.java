package com.codingchili.core.configuration.exception;

import com.codingchili.core.configuration.Strings;
import com.codingchili.core.context.CoreException;

/**
 * @author Robin Duda
 *
 * Throw when a requested file is not present.
 */
public class FileMissingException extends CoreException {
    public FileMissingException(String filename) {
        super(Strings.getFileMissingError(filename));
    }
}
