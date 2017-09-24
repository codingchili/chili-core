package com.codingchili.core.storage.exception;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreException;
import com.codingchili.core.protocol.ResponseStatus;

/**
 * @author Robin Duda
 * <p>
 * Throw when the replace operation cannot complete as there is nothing to replace.
 */
public class NothingToReplaceException extends CoreException {

    public NothingToReplaceException(Object key) {
        super(CoreStrings.getNothingToReplaceException(key.toString()), ResponseStatus.MISSING);
    }
}
