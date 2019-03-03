package com.codingchili.core.files.exception;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreException;
import com.codingchili.core.protocol.ResponseStatus;

/**
 * Throw when a requested file is not present.
 */
public class FileMissingException extends CoreException {
    public FileMissingException(String filename) {
        super(CoreStrings.getFileMissingError(filename), ResponseStatus.MISSING);
    }
}
