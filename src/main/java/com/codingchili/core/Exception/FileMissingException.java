package com.codingchili.core.Exception;

import com.codingchili.core.Configuration.Strings;

/**
 * @author Robin Duda
 */
public class FileMissingException extends CoreException {
    public FileMissingException(String filename) {
        super(Strings.getFileMissingError(filename));
    }
}
