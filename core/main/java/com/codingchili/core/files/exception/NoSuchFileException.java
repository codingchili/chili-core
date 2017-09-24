package com.codingchili.core.files.exception;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 * <p>
 * Throw when a file is missing.
 */
public class NoSuchFileException extends CoreRuntimeException {

    /**
     * @param path the file that was not found on the filesystem.
     */
    protected NoSuchFileException(String path) {
        super(CoreStrings.getFileMissingError(path));
    }
}
