package com.codingchili.core.Exception;

import com.codingchili.core.Configuration.Strings;

/**
 * @author Robin Duda
 */
public class FileReadException extends RuntimeException {

    public FileReadException(String path) {
        super(Strings.getFileReadError(path));
    }
}
