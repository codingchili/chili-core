package com.codingchili.core.storage.exception;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreException;
import com.codingchili.core.protocol.ResponseStatus;

/**
 * Throw when an entity could not be found in storage.
 */
public class ValueMissingException extends CoreException {
    public ValueMissingException(Object key) {
        super(CoreStrings.getMissingEntity(key.toString()), ResponseStatus.MISSING);
    }
}
