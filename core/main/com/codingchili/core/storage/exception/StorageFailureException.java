package com.codingchili.core.storage.exception;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreException;

/**
 * @author Robin Duda
 *
 * Generic storage error, throw when a requested operation has error.
 */
public class StorageFailureException extends CoreException {

    public StorageFailureException() {
        super(CoreStrings.ERROR_STORAGE_EXCEPTION);
    }
}
