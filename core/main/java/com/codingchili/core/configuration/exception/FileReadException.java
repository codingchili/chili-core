package com.codingchili.core.configuration.exception;

import com.codingchili.core.configuration.CoreStrings;

/**
 * @author Robin Duda
 *
 * Throw when an error has occured while reading a file.
 */
public class FileReadException extends RuntimeException {

    public FileReadException(String path) {
        super(CoreStrings.getFileReadError(path));
    }
}
