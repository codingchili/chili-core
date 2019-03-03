package com.codingchili.core.storage.exception;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreException;
import com.codingchili.core.protocol.ResponseStatus;

/**
 * Throw when the replace operation cannot complete as there is no object stored
 * that matches the value of the oject to update.
 */
public class NothingToUpdateException extends CoreException {

    public NothingToUpdateException(String key) {
        super(CoreStrings.getNothingToUpdateException(key), ResponseStatus.MISSING);
    }
}
