package com.codingchili.core.files.exception;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 *         <p>
 *         Throw when failed to write a file.
 */
public class FileWriteException extends CoreRuntimeException {

    /**
     * @param fileName the the target file that failed to save.
     */
    public FileWriteException(String fileName) {
        super(CoreStrings.getFileWriteError(fileName));
    }
}
