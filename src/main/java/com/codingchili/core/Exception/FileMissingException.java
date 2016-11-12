package com.codingchili.core.Exception;

import com.codingchili.core.Configuration.Strings;

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
