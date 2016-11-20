package com.codingchili.core.Exception;

import com.codingchili.core.Configuration.Strings;

/**
 * @author Robin Duda
 *
 * Generic storage error, throw when a requested operation has failed.
 */
public class StorageFailureException extends CoreException {

    public StorageFailureException() {
        super(Strings.ERROR_STORAGE_EXCEPTION);
    }
}
