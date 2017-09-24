package com.codingchili.core.files.exception;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 * <p>
 * Throw when an error has occured while reading a file.
 */
public class FileReadException extends CoreRuntimeException {

    public FileReadException(String path) {
        super(CoreStrings.getFileReadError(path));
    }
}
